package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class VerticalLineChanger extends LineChanger {

    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        int width = imgProcessor.getWidth() + toAdd.length;
        int height = imgProcessor.getHeight();
        boolean[][] updatedPrioritized = new boolean[width][height];
        boolean[][] updatedProtected = new boolean[width][height];
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
                updatedPrioritized[x][y] = prioritizedPixels[x - shift][y];
                updatedProtected[x][y] = protectedPixels[x - shift][y];
                newIp.putPixel(x, y, imgProcessor.getPixel(x - shift, y));

                if (shift < toAdd.length && toAdd[shift][y] == x) {
                    shift += 1;
                }
            }

            for (int[] row : toAdd) {
                newIp.putPixel(row[y] + 1, y, average(newIp.getPixel(row[y], y), newIp.getPixel(row[y] + 2, y), gray8));
            }
        }

        prioritizedPixels = updatedPrioritized;
        protectedPixels = updatedProtected;
        return newIp;
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        int width = imgProcessor.getWidth() - toRemove.length;
        int height = imgProcessor.getHeight();
        boolean[][] updatedPrioritized = new boolean[width][height];
        boolean[][] updatedProtected = new boolean[width][height];

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
                updatedPrioritized[x - shift][y] = prioritizedPixels[x][y];
                updatedProtected[x - shift][y] = protectedPixels[x][y];
                newIp.putPixel(x - shift, y, imgProcessor.getPixel(x, y));
            }
        }

        this.prioritizedPixels = updatedPrioritized;
        this.protectedPixels = updatedProtected;
        return newIp;
    }
}
