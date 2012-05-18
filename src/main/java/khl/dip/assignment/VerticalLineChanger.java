package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class VerticalLineChanger extends LineChanger {

    // TODO: actually implement this - current code is wonky at best
    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        boolean gray8;

        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth() + 1, imgProcessor.getHeight());
            gray8 = false;
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth() + 1, imgProcessor.getHeight());
            gray8 = true;
        } else {
            throw new UnsupportedOperationException();
        }

        int shift;
        for (int y = 0; y < imgProcessor.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < imgProcessor.getWidth(); x++) {
                if (toAdd[shift][y] == x) {
                    shift += 1;
                }

                newIp.putPixel(x, y, imgProcessor.getPixel(x - shift, y));
            }

            // TODO: this should loop through the toAdd values, not just take the last one
            newIp.putPixel(toAdd[shift][y] + 1, y, average(imgProcessor.getPixel(toAdd[shift][y], y), imgProcessor.getPixel(toAdd[shift][y] + 1, y), gray8));
        }

        return newIp;
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        int width = imgProcessor.getWidth() - toRemove.length;
        int height = imgProcessor.getHeight();
        int[][] updatedPrioritized = new int[width][height];

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
                    // skip line, we're removing it
                    
                } else {
                    updatedPrioritized[x-shift][y] = prioritizedPixels[x][y]; 
                    newIp.putPixel(x - shift, y, imgProcessor.getPixel(x, y));
                }
            }
        }

        this.prioritizedPixels = updatedPrioritized;
        return newIp;
    }
}
