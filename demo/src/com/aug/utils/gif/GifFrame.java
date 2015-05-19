package com.aug.utils.gif;

import android.graphics.Bitmap;


public class GifFrame {
    public GifFrame(Bitmap im, int del) {
        image = im;
        if (del < 20) {
            del = 20;
        }
        delay = del;
    }
    public Bitmap image;
    public int delay;
}
