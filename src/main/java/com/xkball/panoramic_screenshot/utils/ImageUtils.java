package com.xkball.panoramic_screenshot.utils;

import com.mojang.blaze3d.platform.NativeImage;

import java.awt.image.BufferedImage;

public class ImageUtils {
    
    public static BufferedImage toBufferedImage(NativeImage image){
        var w = image.getWidth();
        var h = image.getHeight();
        var result = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                result.setRGB(i,j,image.getPixel(i,j));
            }
        }
        return result;
    }
}
