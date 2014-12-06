
package com.aug.utils.gif;

import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class GifPlayer {
    public interface GifPlayListener {
        void onNextBitmapReady(Bitmap bmp);
        void onPlayFinish();
        void onLoop();
    }

    private static final int GIF_FRAME_READY_MSG = 1;

    private static final int IS_GIF_UNKNOWN = 0;
    private static final int IS_GIF_YES = 1;
    private static final int IS_GIF_NO = 2;
    private static final int IS_GIF_FILE_NOT_FOUND = 3;
    private int mDecodeStatus = IS_GIF_UNKNOWN;

    private RealTimeGifDecoder gifDecoder;
    private Handler handler = null;
    private AtomicBoolean mIsPlaying = new AtomicBoolean(false);
    private GifPlayListener mGifPlayListener;
    private boolean mLoopPlay = true;
    private boolean mIsLooped = false;

    public boolean play(String filePath, boolean startPlayNow) {
        boolean canPlay = false;
        try {
            gifDecoder = new RealTimeGifDecoder(filePath);
            gifDecoder.setLoop(mLoopPlay);
            canPlay = gifDecoder.isGif();
            mDecodeStatus = canPlay ? IS_GIF_YES : IS_GIF_NO;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mDecodeStatus = IS_GIF_FILE_NOT_FOUND;
        } finally {
            if (!canPlay) {
                if (gifDecoder != null) {
                    gifDecoder.onDestroy();
                    gifDecoder = null;
                }
            }
        }

        if (canPlay) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    showNextGifFrame();
                }
            };
            if (startPlayNow) {
                start();
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
        if (handler != null) {
            handler.removeMessages(GIF_FRAME_READY_MSG);
            handler = null;
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
            
            handler.sendEmptyMessageDelayed(GIF_FRAME_READY_MSG, frame.delay);
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
