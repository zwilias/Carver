
package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class VerticalLineChanger extends LineChanger {
    @Override
    public ImageProcessor addLine(int[][] toAdd, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        boolean gray8;
        
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth()+1, imgProcessor.getHeight());
            gray8 = false;
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth()+1, imgProcessor.getHeight());
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
                
                newIp.putPixel(x, y, imgProcessor.getPixel(x-shift, y));
            }
            
            // TODO: this should loop through the toAdd values, not just take the last one
            newIp.putPixel(toAdd[shift][y]+1, y, average(imgProcessor.getPixel(toAdd[shift][y], y), imgProcessor.getPixel(toAdd[shift][y]+1, y), gray8));
        }
        
        return newIp;
    }
    
    private int average(int color1, int color2, boolean gray8) {
        int result;
        
        if (gray8) {
            result = (color1 + color2)/2;
        } else {
            int r = (((color1 & 0xff0000) >> 16) + ((color2 & 0xff0000) >> 16)) / 2;
            int g = (((color1 & 0xff00) >> 8) + ((color2 & 0xff00) >> 8)) / 2;
            int b = ((color1 & 0xff) + (color2 & 0xff)) / 2;
            
            result = r << 16 | g << 8 | b;
        }
        
        return result;
    }

    @Override
    public ImageProcessor removeLine(int[][] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth()-toRemove.length, imgProcessor.getHeight());
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth()-toRemove.length, imgProcessor.getHeight());
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
                
                newIp.putPixel(x-shift, y, imgProcessor.getPixel(x, y));
            }
        }
        
        return newIp;
    }

}
