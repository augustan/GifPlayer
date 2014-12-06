
package com.aug.gifplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.aug.utils.gif.GifPlayer;
import com.aug.utils.gif.GifPlayer.GifPlayListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements GifPlayListener {

    private int i = 0;
    private ImageView iv;
    private GifPlayer mGifPlayer;
    private Button stop_btn;
    private Button next_btn;
    
    private int fileId = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGifPlayer = new GifPlayer();
        
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.image_view);
        stop_btn = (Button) findViewById(R.id.stop_btn);
        next_btn = (Button) findViewById(R.id.next_btn);
        
        stop_btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mGifPlayer != null) {
                    mGifPlayer.stop();
                }
            }
        });
        next_btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                fileId = 2 - (fileId + 1) % 2;
                if (mGifPlayer != null) {
                    mGifPlayer.stop();
                    playFile(fileId);
                }
            }
        });
        
        playFile(fileId);
    }
    
    private void playFile(int id) {
        copyTestGifFile(id);
        
        String gifFile = getCacheRootDir() + "/" + id + ".gif";
        mGifPlayer.setGifPlayListener(this);
//        mGifPlayer.setLoopPlay(false);
        mGifPlayer.play(gifFile, true);
    }
    
    private void copyTestGifFile(int id) {
        String from = id + ".gif";
        String to = getCacheRootDir() + "/" + from;

        File toFile = new File(to);
        InputStream source = null;
        OutputStream destination = null;
        if (!toFile.exists()) {
            try {
                toFile.getParentFile().mkdirs();

                source = getAssets().open(from);
                destination = new FileOutputStream(toFile);
                byte[] buffer = new byte[10240];
                int nread = 0;

                while ((nread = source.read(buffer)) != -1) {
                    if (nread == 0) {
                        nread = source.read();
                        if (nread < 0) {
                            break;
                        }
                        destination.write(nread);
                        continue;
                    }
                    destination.write(buffer, 0, nread);
                }
                destination.close();
            } catch (IOException e) {
            } finally {
                if (source != null) {
                    try {
                        source.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (destination != null) {
                    try {
                        destination.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onNextBitmapReady(Bitmap bmp) {
        i++;
//        ImageUtil.saveBitmap(bmp, getCacheRootDir() + "decode/" + i + ".bmp", ImageUtil.QUALITY_HIGH);
        iv.setImageBitmap(bmp);
    }
    
    @Override
    protected void onDestroy() {
        mGifPlayer.stop();
        super.onDestroy();
    }

    @Override
    public void onPlayFinish() {
        i = 0;
    }

    @Override
    public void onLoop() {
        i = 0;
    }

    public static String getCacheRootDir() {
        return android.os.Environment.getExternalStorageDirectory() + "/aug/gif";
    }

}
