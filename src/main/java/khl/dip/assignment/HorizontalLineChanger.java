package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class HorizontalLineChanger extends LineChanger {

    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imageProcessor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;

        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth(), imgProcessor.getHeight() - toRemove.length);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth(), imgProcessor.getHeight() - toRemove.length);
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

                newIp.putPixel(x, y - shift, imgProcessor.getPixel(x, y));
            }
        }

        return newIp;
    }
}
