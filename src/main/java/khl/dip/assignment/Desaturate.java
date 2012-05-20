package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Desaturate {
    /*
     * In this case, we apply it on a duplicate and return it instead of
     * applying it in-place. Keep this in mind.
     */

    public int[][] applyTo(final ImageProcessor ip) {
        final int[][] result = new int[ip.getWidth()][ip.getHeight()];

        if (ip instanceof ColorProcessor) {
            for (int x = 0; x < ip.getWidth(); x++) {
                for (int y = 0; y < ip.getHeight(); y++) {
                    result[x][y] = desaturate(ip.getPixel(x, y));
                }
            }
        } else if (ip instanceof ByteProcessor) {
            for (int x = 0; x < ip.getWidth(); x++) {
                for (int y = 0; y < ip.getHeight(); y++) {
                    result[x][y] = ip.getPixel(x, y);
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }

        return result;
    }

    public int desaturate(final int color) {
        return (int) (0.21 * ((color & 0xff0000) >> 16) + 0.72 * ((color & 0x00ff00) >> 8) + 0.07 * (color & 0x0000ff));
    }
}
