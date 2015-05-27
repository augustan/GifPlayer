
package com.aug.utils.gif;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.atomic.AtomicBoolean;

public class GifPlayer {
    public interface GifPlayListener {
        void onNextBitmapReady(Bitmap bmp);

        void onPlayFinish();

        void onLoop();
    }

    private static final int GIF_FRAME_READY_MSG = 999;

    private static final int IS_GIF_UNKNOWN = 0;
    private static final int IS_GIF_YES = 1;
    private static final int IS_GIF_NO = 2;
    private static final int IS_GIF_FILE_NOT_FOUND = 3;
    private int mDecodeStatus = IS_GIF_UNKNOWN;

    private RealTimeGifDecoder gifDecoder;
    private Handler manThreadHandler = null;
    private AtomicBoolean mIsPlaying = new AtomicBoolean(false);
    private GifPlayListener mGifPlayListener;
    private boolean mLoopPlay = true;
    private boolean mIsLooped = false;

    public boolean play(String filePath, boolean startPlayNow) {
        byte[] data = null;
        data = FileUtils.getFileBytesFrom(filePath);
        return play(data, startPlayNow);
    }

    public boolean play(byte[] data, boolean startPlayNow) {
        boolean canPlay = false;
        if (data != null) {
            try {
                // gifDecoder = new RealTimeGifDecoder(filePath);
                gifDecoder = new RealTimeGifDecoder(data);
                gifDecoder.setLoop(mLoopPlay);
                canPlay = gifDecoder.isGif();
                mDecodeStatus = canPlay ? IS_GIF_YES : IS_GIF_NO;
            } catch (Exception e) {
                e.printStackTrace();
                mDecodeStatus = IS_GIF_FILE_NOT_FOUND;
            }
        }

        if (canPlay) {
            manThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == GIF_FRAME_READY_MSG) {
                        showNextGifFrame();
                    }
                }
            };
            if (startPlayNow) {
                start();
            }
        } else {
            if (gifDecoder != null) {
                gifDecoder.onDestroy();
                gifDecoder = null;
            }

            if (data != null) {
                Bitmap bmp = FileUtils.decodeBitmap(data);
                mGifPlayListener.onNextBitmapReady(bmp);
            }
        }
        return canPlay;
    }

    public void start() {
        if (mDecodeStatus == IS_GIF_YES && !mIsPlaying.getAndSet(true)) {
            showNextGifFrame();
        }
    }

    public void stop() {
        reset();
    }

    public boolean canTryPlayGif() {
        return mDecodeStatus != IS_GIF_NO;
    }

    private void reset() {
        mIsPlaying.set(false);
        mIsLooped = false;
        mDecodeStatus = IS_GIF_UNKNOWN;
        if (gifDecoder != null) {
            gifDecoder.onDestroy();
            gifDecoder = null;
        }
        if (manThreadHandler != null) {
            manThreadHandler.removeMessages(GIF_FRAME_READY_MSG);
            manThreadHandler = null;
        }
    }

    private void showNextGifFrame() {
        if (gifDecoder == null) {
            return;
        }
        GifFrame frame = gifDecoder.getNextBitmap();
        if (frame != null && frame.image != null) {
            if (mIsLooped) {
                mIsLooped = false;
                gifDecoder.resetIsLooped();
                if (mGifPlayListener != null) {
                    mGifPlayListener.onLoop();
                }
            }

            // mIsLooped = true后，调用这里时，image已经是第一帧了
            if (mGifPlayListener != null) {
                mGifPlayListener.onNextBitmapReady(frame.image);
            }

            manThreadHandler.sendEmptyMessageDelayed(GIF_FRAME_READY_MSG, frame.delay);
            mIsLooped = gifDecoder.prepareNextBitmap();
        } else {
            if (mGifPlayListener != null) {
                mGifPlayListener.onPlayFinish();
            }
        }
    }

    public int getDecodeStatus() {
        return mDecodeStatus;
    }

    public boolean isPlaying() {
        return mIsPlaying.get();
    }

    public GifPlayListener getGifPlayListener() {
        return mGifPlayListener;
    }

    public void setGifPlayListener(GifPlayListener gifPlayListener) {
        this.mGifPlayListener = gifPlayListener;
    }

    public boolean isLoopPlay() {
        return mLoopPlay;
    }

    public void setLoopPlay(boolean loopPlay) {
        this.mLoopPlay = loopPlay;
    }
}
