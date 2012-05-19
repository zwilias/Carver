package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class HorizontalLineChanger extends LineChanger {

    //TODO: implement this
    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        int width = imgProcessor.getWidth();
        int height = imgProcessor.getHeight() + toAdd.length;
        boolean[][] updatedPrioritized = new boolean[width][height];
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
                updatedPrioritized[x][y] = prioritizedPixels[x][y - shift];
                newIp.putPixel(x, y, imgProcessor.getPixel(x, y - shift));
                                                
                if (shift < toAdd.length && toAdd[shift][x] == y) {
                    shift += 1;
                }
            }

            for (int[] row : toAdd) {
                newIp.putPixel(x, row[x] + 1, average(newIp.getPixel(x, row[x]), newIp.getPixel(x, row[x] + 2), gray8));
            }    
        }

        prioritizedPixels = updatedPrioritized;
        return newIp;
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        int width = imgProcessor.getWidth();
        int height = imgProcessor.getHeight() - toRemove.length;
        ImageProcessor newIp;
        boolean[][] updatedPrioritized = new boolean[width][height];

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

                updatedPrioritized[x][y-shift] = prioritizedPixels[x][y];
                newIp.putPixel(x, y - shift, imgProcessor.getPixel(x, y));
            }
        }

        this.prioritizedPixels = updatedPrioritized;
        return newIp;
    }
}
