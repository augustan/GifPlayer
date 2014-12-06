
package com.aug.gifplayer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
    public static final int QUALITY_HIGH = 85;
    public static final int QUALITY_MIDDLE = 75;
    public static final int QUALITY_LOW = 70;

    public static boolean saveBitmap(Bitmap bitmap, String path, int quality) {
        if (bitmap == null || path == null || path.equals("")) {
            return false;
        }
        BufferedOutputStream ostream = null;
        try {
            File file = new File(path);
            makeDIRAndCreateFile(path);
            ostream = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(CompressFormat.JPEG, quality, ostream);
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (ostream != null) {
                    ostream.flush();
                    ostream.close();
                }
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static synchronized File makeDIRAndCreateFile(String filePath) throws Exception {
        File file = new File(filePath);
        String parent = file.getParent();
        File parentFile = new File(parent);
        if (!parentFile.exists()) {
            if (parentFile.mkdirs()) {
                file.createNewFile();
            } else {
                throw new IOException();
            }
        } else {
            if (!file.exists()) {
                file.createNewFile();
            }
        }
        return file;
    }
}
