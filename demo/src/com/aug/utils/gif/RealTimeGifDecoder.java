
package com.aug.utils.gif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RealTimeGifDecoder extends GifDecoder {

    private String mGifFilePath;
    private File mGifFile;
    private byte[] fileData;
    private boolean decodedGif = false; // 至少可解析出一张图像
    private boolean mLoop = true;

    private GifFrame preparedNextBitmap = null;
    private boolean mFinishOneRound = false;

    public RealTimeGifDecoder(String gifFilePath) throws FileNotFoundException {
        mGifFilePath = gifFilePath;
        mGifFile = new File(mGifFilePath);
        byte[] data = FileUtils.getFileBytesFrom(mGifFile);
        fileData = data;
        prepareRead();
    }

    public RealTimeGifDecoder(byte[] data) {
        fileData = data;
        prepareRead();
    }

    public boolean isGif() {
        return status == STATUS_OK;
    }

    synchronized public boolean isLoop() {
        return mLoop;
    }

    synchronized public void setLoop(boolean loop) {
        this.mLoop = loop;
    }

    synchronized public GifFrame getNextBitmap() {
        GifFrame next = preparedNextBitmap;
        if (next == null) {
            return getNextBitmap(mLoop);
        } else {
            preparedNextBitmap = null;
            return next;
        }
    }

    synchronized public boolean prepareNextBitmap() {
        if (mLoop || !mFinishOneRound) {
            preparedNextBitmap = getNextBitmap(mLoop);
        }
        return mFinishOneRound;
    }

    synchronized public void onDestroy() {
        decodedGif = false;
        preparedNextBitmap = null;
        lastImage = null;
        FileUtils.closeInStream(in);
    }

    public boolean isLooped() {
        return mFinishOneRound;
    }

    public void resetIsLooped() {
        mFinishOneRound = false;
    }

    /**
     * 预读取gif。判断格式，不解析数据
     * 
     * @param InputStream containing GIF file.
     * @return read status code (0 = no errors)
     */
    private int prepareRead() {
        init();
        InputStream is = null;
        if (fileData != null) {
            is = new ByteArrayInputStream(fileData);
        }
        if (is != null) {
            if (!(is instanceof BufferedInputStream)) {
                is = new BufferedInputStream(is);
            }
            in = (BufferedInputStream) is;
            readHeader();
            if (err()) {
                status = STATUS_FORMAT_ERROR;
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }
        return status;
    }

    private void reset() {
        onDestroy();
        prepareRead();
    }

    private GifFrame getNextBitmap(boolean loop) {
        GifFrame frame = getNextFrame(loop);
        if (frame == null && loop && decodedGif) {
            // 发生了错误，但是可以显示之前的几张图像
            reset();
            frame = getNextFrame(loop);
        }
        if (!decodedGif && frame != null) {
            decodedGif = true;
        }
        return frame;
    }

    /**
     * 获取下一帧图片
     * 
     * @param loop false：读到结尾就返回null。true：到结尾后返回第一帧֡
     * @return
     */
    private GifFrame getNextFrame(boolean loop) {
        GifFrame ret = null;

        try {
            // read GIF file content blocks
            boolean done = false; // 标识读取一个frame结束
            boolean finish = false; // 标识gif播放到结尾
            while (!(done || err())) {
                int code = read();
                switch (code) {
                    case 0x2C: // image separator
                        ret = readImageBlock();
                        done = true;
                        break;
                    case 0x21: // extension
                        code = read();
                        switch (code) {
                            case 0xf9: // graphics control extension
                                readGraphicControlExt();
                                break;
                            case 0xff: // application extension
                                readBlock();
                                String app = "";
                                for (int i = 0; i < 11; i++) {
                                    app += (char) block[i];
                                }
                                if (app.equals("NETSCAPE2.0")) {
                                    readNetscapeExt();
                                } else
                                    skip(); // don't care
                                break;
                            default: // uninteresting extension
                                skip();
                        }
                        break;
                    case 0x3b: // terminator
                        finish = true;
                        done = true;
                        mFinishOneRound = true;
                        break;
                    case 0x00: // bad byte, but keep going and see what happens
                        break;
                    default:
                        status = STATUS_FORMAT_ERROR;
                }
            }
            if (err()) {
                ret = null;
            }

            if (finish && loop) {
                reset();
                ret = getNextFrame(false);
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * 解码一帧图像
     * 
     * @return
     */
    private GifFrame readImageBlock() {
        GifFrame ret = null;

        ix = readShort(); // (sub)image position & size
        iy = readShort();
        iw = readShort();
        ih = readShort();

        int packed = read();
        lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
        interlace = (packed & 0x40) != 0; // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        lctSize = 2 << (packed & 7); // 6-8 - local color table size

        if (lctFlag) {
            lct = readColorTable(lctSize); // read table
            act = lct; // make local table active
        } else {
            act = gct; // make global table active
            if (bgIndex == transIndex)
                bgColor = 0;
        }
        int save = 0;
        if (transparency) {
            save = act[transIndex];
            act[transIndex] = 0; // set transparent color if specified
        }

        if (act == null) {
            status = STATUS_FORMAT_ERROR; // no color table defined
        }

        if (err())
            return ret;

        decodeImageData(); // decode pixel data
        skip();

        if (err())
            return ret;

        // create new image to receive frame data
        image = Bitmap.createBitmap(width, height, Config.ARGB_4444);

        setPixels(); // transfer pixel data to image

        ret = new GifFrame(image, delay);

        int[] dest = new int[width * height];
        image.getPixels(dest, 0, width, 0, 0, width, height);

        if (transparency) {
            act[transIndex] = save;
        }
        resetFrame();

        return ret;
    }
}
