package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class HorizontalLineChanger extends LineChanger {

    //TODO: implement this
    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imageProcessor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        int width = imgProcessor.getWidth();
        int height = imgProcessor.getHeight() - toRemove.length;
        ImageProcessor newIp;
        int[][] updatedPrioritized = new int[width][height];

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

        return newIp;
    }
}
