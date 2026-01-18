package com.kevsterking.imagemod.core.ImageBuilder;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class ResizeableImage {

    public BufferedImage rootImage;

    public int[] buffer;
    public int   width;
    public int   height;

    public ResizeableImage(BufferedImage img) {
        this.rootImage  = ensure_type(img);
        this.buffer     = ResizeableImage.copy_buffer(this.rootImage.getData().getDataBuffer());
        this.width      = img.getWidth();
        this.height     = img.getHeight();
    }
    private ResizeableImage(BufferedImage rootImage, int[] buf, final int w, final int h) {
        this.rootImage  = rootImage;
        this.buffer     = buf;
        this.width      = w;
        this.height     = h;
    }

    public static ResizeableImage resize(ResizeableImage img, final int w, final int h) {
        BufferedImage resizedImage = new BufferedImage(w, h, img.rootImage.getType());
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(img.rootImage, 0, 0, w, h, null);
        graphics2D.dispose();
        return new ResizeableImage(resizedImage);
    }

    public static ResizeableImage get_transparent(final int w, final int h) {
        BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = ret.createGraphics();
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.drawRect(0,0, w, h);
        g2d.drawImage(ret, 0, 0, null);
        return new ResizeableImage(ret);
    }

    public ResizeableImage sub_image(final int x, final int y, final int w, final int h) {
        int[] ret = new int[w*h*4];
        int idx = 0;
        for (int yy = y; yy < y+h; yy++) {
            for (int xx = x; xx < x+w; xx++) {
                for (int b = 0; b < 4; b++) {
                    int index = 4*(xx+(this.height-yy-1)*this.width) + b;
                    ret[idx] = this.buffer[index];
                    idx++;
                }
            }
        }
        return new ResizeableImage(this.rootImage, ret, w, h);
    }

    public int get_similarity(ResizeableImage img) {
        int ret = 0;
        if (img.buffer.length != this.buffer.length) {
            return Integer.MAX_VALUE;
        }
        for (int i = 0; i < img.buffer.length; i+=4) {
            double color_factor = (double) Math.min(img.buffer[i], this.buffer[i]) / 255;
            int dalpha = Math.abs(img.buffer[i]   - this.buffer[i]) * 3;
            int db     = Math.abs(img.buffer[i+1] - this.buffer[i+1]);
            int dg     = Math.abs(img.buffer[i+2] - this.buffer[i+2]);
            int dr     = Math.abs(img.buffer[i+3] - this.buffer[i+3]);
            ret += (int) ((1.0 - color_factor) * dalpha + color_factor * (db + dg + dr));
        }
        return ret;
    }

    private static int[] copy_buffer(DataBuffer buf) {
        int[] ret = new int[buf.getSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = buf.getElem(i);
        }
        return ret;
    }

    private BufferedImage ensure_type(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            converted.getGraphics().drawImage(image, 0, 0, null);
            return converted;
        }
        return image;
    }

}
