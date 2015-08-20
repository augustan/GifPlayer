
package com.aug.utils.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static byte[] getFileBytesFrom(String filePath) {
        return getFileBytesFrom(new File(filePath));
    }

    public static byte[] getFileBytesFromAssets(Context c, String filePath) {
        InputStream stream = null;
        try {
            stream = c.getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getFileBytesFrom(stream);
    }
    
    public static Bitmap decodeBitmap(byte [] data) {
        Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
        return bm;
    }
    
    public static byte[] getFileBytesFrom(File file) {
        byte[] data = null;
        if (file == null) {
            return data;
        }
        
        try {
            data = getFileBytesFrom(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static byte[] getFileBytesFrom(InputStream stream) {
        byte[] data = null;
        if (stream == null) {
            return data;
        }
        
        ByteArrayOutputStream out = null;
        int len = 4096 * 4;
        try {
            out = new ByteArrayOutputStream(len);
            byte[] b = new byte[len];
            int n;
            while ((n = stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
            data = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeInStream(stream);
            closeOutStream(out);
        }
        return data;
    }
    
    public static void closeInStream(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeOutStream(OutputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
