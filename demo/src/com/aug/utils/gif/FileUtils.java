
package com.aug.utils.gif;

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
    
    public static Bitmap decodeBitmap(byte [] data) {
        Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
        return bm;
    }
    
    public static byte[] getFileBytesFrom(File file) {
        byte[] data = null;
        if (file == null) {
            return data;
        }

        FileInputStream stream = null;
        ByteArrayOutputStream out = null;
        try {
            int len = (int) file.length();
            stream = new FileInputStream(file);
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
