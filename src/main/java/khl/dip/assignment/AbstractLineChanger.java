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
public abstract class AbstractLineChanger {

    public boolean[][] prioritizedPixels;
    public boolean[][] protectedPixels;

    public ImageProcessor changeLine(final int[][] toChange, final ImageProcessor imageProcessor, final boolean addLines, final boolean plotLines, final boolean[][] prioritizedPixels, final boolean[][] protectedPixels) {
        this.prioritizedPixels = prioritizedPixels;
        this.protectedPixels = protectedPixels;
        ImageProcessor result;

        if (plotLines) {
            result = plotLine(toChange, imageProcessor);
        } else if (addLines) {
            result = addLine(toChange, imageProcessor);
        } else {
            result = removeLine(toChange, imageProcessor);
        }
        
        return result;
    }

    public abstract ImageProcessor addLine(int[][] toAdd, ImageProcessor imageProcessor);

    public abstract ImageProcessor removeLine(int[][] toRemove, ImageProcessor imageProcessor);
    
    public abstract ImageProcessor plotLine(int[][] toPlot, ImageProcessor imageProcessor);

    protected int average(final int color1, final int color2, final boolean gray8) {
        int result;
        if (gray8) {
            result = (color1 + color2) / 2;
        } else {
            final int red = (((color1 & 16711680) >> 16) + ((color2 & 16711680) >> 16)) / 2;
            final int green = (((color1 & 65280) >> 8) + ((color2 & 65280) >> 8)) / 2;
            final int blue = ((color1 & 255) + (color2 & 255)) / 2;
            result = red << 16 | green << 8 | blue;
        }
        return result;
    }
}
