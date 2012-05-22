package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class HorizontalLineChanger extends AbstractLineChanger {

    @Override
    public ImageProcessor addLine(final int[][] toAdd, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth();
        final int height = imgProcessor.getHeight() + toAdd.length;
        boolean[][] updatedPrio = new boolean[width][height];
        boolean[][] updatedProt = new boolean[width][height];
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
        for (int x = 0; x < newIp.getWidth(); x++) {
            shift = 0;
            for (int y = 0; y < newIp.getHeight(); y++) {
                updatedPrio[x][y] = prioritizedPixels[x][y - shift];
                updatedProt[x][y] = protectedPixels[x][y - shift];
                newIp.putPixel(x, y, imgProcessor.getPixel(x, y - shift));

                if (shift < toAdd.length && toAdd[shift][x] == y) {
                    shift += 1;
                }
            }

            for (int[] row : toAdd) {
                newIp.putPixel(x, row[x] + 1, average(newIp.getPixel(x, row[x]), newIp.getPixel(x, row[x] + 2), gray8));
            }
        }

        prioritizedPixels = updatedPrio;
        protectedPixels = updatedProt;
        return newIp;
    }

    @Override
    public ImageProcessor removeLine(final int[][] toRemove, final ImageProcessor imgProcessor) {
        final int width = imgProcessor.getWidth();
        final int height = imgProcessor.getHeight() - toRemove.length;
        ImageProcessor newIp;
        boolean[][] updatedPrio = new boolean[width][height];
        boolean[][] updatedProt = new boolean[width][height];

        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(width, height);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException();
        }

        int shift;
        for (int x = 0; x < imgProcessor.getWidth(); x++) {
            shift = 0;
            for (int y = 0; y < imgProcessor.getHeight(); y++) {
                if (shift < toRemove.length && toRemove[shift][x] == y) {
                    shift++;
                    continue;
                }

                updatedPrio[x][y - shift] = prioritizedPixels[x][y];
                updatedProt[x][y - shift] = protectedPixels[x][y];
                newIp.putPixel(x, y - shift, imgProcessor.getPixel(x, y));
            }
        }

        this.prioritizedPixels = updatedPrio;
        this.protectedPixels = updatedProt;
        return newIp;
    }
    
    @Override
    public ImageProcessor markLine(final int[][] toMark, final ImageProcessor imgProcessor) {
        final int width = imgProcessor.getWidth();
        final int height = imgProcessor.getHeight();
        ImageProcessor newIp;

        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(width, height);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException();
        }

        int shift;
        for (int x = 0; x < imgProcessor.getWidth(); x++) {
            shift = 0;
            for (int y = 0; y < imgProcessor.getHeight(); y++) {
                if (shift < toMark.length && toMark[shift][x] == y) {
                    shift++;
                    newIp.putPixel(x, y, 255<<16);
                } else {
                    newIp.putPixel(x, y, imgProcessor.getPixel(x, y));

                }
            }
        }

        return newIp;
    }
}
