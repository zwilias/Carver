/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 *
 * @author ilias
 */
public class LineChanger {

    public boolean[][] prioritizedPixels;
    public boolean[][] protectedPixels;
    public int[][] grayimg;

    public ImageProcessor changeLine(final int[][] toChange, final ImageProcessor imageProcessor, final boolean addLines, final boolean plotLines, final boolean[][] prioritizedPixels, final boolean[][] protectedPixels, final int[][] grayimg) {
        this.prioritizedPixels = prioritizedPixels;
        this.protectedPixels = protectedPixels;
        this.grayimg = grayimg;
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

    public ImageProcessor addLine(final int[][] toAdd, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth() + toAdd.length;
        final int height = imgProcessor.getHeight();
        boolean[][] updatedPrio = new boolean[width][height];
        boolean[][] updatedProt = new boolean[width][height];
        int[][] updatedGray = new int[width][height];
        boolean gray8;
        
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(width, height);
            gray8 = false;
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(width, height);
            gray8 = true;
        } else {
            throw new UnsupportedOperationException();
        }
        int shift;
        for (int y = 0; y < newIp.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < newIp.getWidth(); x++) {
                updatedPrio[x][y] = prioritizedPixels[x - shift][y];
                updatedProt[x][y] = protectedPixels[x - shift][y];
                newIp.putPixel(x, y, imgProcessor.getPixel(x - shift, y));
                updatedGray[x][y] = grayimg[x-shift][y];
                if (shift < toAdd.length && toAdd[shift][y] == x) {
                    shift += 1;
                }
            }
            for (int[] row : toAdd) {
                newIp.putPixel(row[y] + 1, y, average(newIp.getPixel(row[y], y), newIp.getPixel(row[y] + 2, y), gray8));
                updatedGray[row[y] + 1][y] = average(updatedGray[row[y]][y], updatedGray[row[y] + 2][y], true);
                updatedProt[row[y] + 1][y] = true;
            }
        }
        prioritizedPixels = updatedPrio;
        protectedPixels = updatedProt;
        grayimg = updatedGray;
        return newIp;
    }

    public ImageProcessor plotLine(final int[][] toPlot, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth();
        final int height = imgProcessor.getHeight();
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(width, height);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException();
        }
        int shift;
        for (int y = 0; y < imgProcessor.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < imgProcessor.getWidth(); x++) {
                if (shift < toPlot.length && toPlot[shift][y] == x) {
                    shift += 1;
                    newIp.putPixel(x, y, 255 << 16);
                } else {
                    newIp.putPixel(x, y, imgProcessor.getPixel(x, y));
                }
            }
        }
        return newIp;
    }

    public ImageProcessor removeLine(final int[][] toRemove, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth() - toRemove.length;
        final int height = imgProcessor.getHeight();
        boolean[][] updatedPrio = new boolean[width][height];
        boolean[][] updatedProt = new boolean[width][height];
        int[][] updatedGray = new int[width][height];
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(width, height);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException();
        }
        int shift;
        for (int y = 0; y < imgProcessor.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < imgProcessor.getWidth(); x++) {
                if (shift < toRemove.length && toRemove[shift][y] == x) {
                    shift += 1;
                    continue;
                }
                updatedPrio[x - shift][y] = prioritizedPixels[x][y];
                updatedProt[x - shift][y] = protectedPixels[x][y];
                newIp.putPixel(x - shift, y, imgProcessor.getPixel(x, y));
                updatedGray[x-shift][y] = grayimg[x][y];
            }
        }
        this.prioritizedPixels = updatedPrio;
        this.protectedPixels = updatedProt;
        this.grayimg = updatedGray;
        return newIp;
    }
}
