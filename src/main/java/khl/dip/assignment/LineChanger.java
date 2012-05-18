/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package khl.dip.assignment;

import ij.process.ImageProcessor;

/**
 *
 * @author ilias
 */
public abstract class LineChanger {
    public int[][] prioritizedPixels;

    public ImageProcessor changeLine(int[][] toChange, ImageProcessor imageProcessor, boolean addLines, int[][] prioritizedPixels) {
        this.prioritizedPixels = prioritizedPixels;
        
        if (addLines) {
            return addLine(toChange, imageProcessor);
        } else {
            return removeLine(toChange, imageProcessor);
        }
    }

    public abstract ImageProcessor addLine(int[][] toAdd, ImageProcessor imageProcessor);

    public abstract ImageProcessor removeLine(int[][] toRemove, ImageProcessor imageProcessor);

    protected int average(int color1, int color2, boolean gray8) {
        int result;
        if (gray8) {
            result = (color1 + color2) / 2;
        } else {
            int r = (((color1 & 16711680) >> 16) + ((color2 & 16711680) >> 16)) / 2;
            int g = (((color1 & 65280) >> 8) + ((color2 & 65280) >> 8)) / 2;
            int b = ((color1 & 255) + (color2 & 255)) / 2;
            result = r << 16 | g << 8 | b;
        }
        return result;
    }
}
