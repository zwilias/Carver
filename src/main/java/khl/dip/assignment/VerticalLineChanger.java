package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class VerticalLineChanger extends AbstractLineChanger {

    @Override
    public ImageProcessor addLine(final int[][] toAdd, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth() + toAdd.length;
        final int height = imgProcessor.getHeight();
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
        for (int y = 0; y < newIp.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < newIp.getWidth(); x++) {
                updatedPrio[x][y] = prioritizedPixels[x - shift][y];
                updatedProt[x][y] = protectedPixels[x - shift][y];
                newIp.putPixel(x, y, imgProcessor.getPixel(x - shift, y));

                if (shift < toAdd.length && toAdd[shift][y] == x) {
                    shift += 1;
                }
            }

            for (int[] row : toAdd) {
                newIp.putPixel(row[y] + 1, y, average(newIp.getPixel(row[y], y), newIp.getPixel(row[y] + 2, y), gray8));
            }
        }

        prioritizedPixels = updatedPrio;
        protectedPixels = updatedProt;
        return newIp;
    }

    @Override
    public ImageProcessor removeLine(final int[][] toRemove, final ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        final int width = imgProcessor.getWidth() - toRemove.length;
        final int height = imgProcessor.getHeight();
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
            }
        }

        this.prioritizedPixels = updatedPrio;
        this.protectedPixels = updatedProt;
        return newIp;
    }
    
    
    @Override
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
                    newIp.putPixel(x, y, 255<<16);
                } else {
                    newIp.putPixel(x, y, imgProcessor.getPixel(x, y));
                }
            }
        }

        return newIp;
    }
}
