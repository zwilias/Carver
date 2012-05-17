
package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class VerticalLineRemover implements LineRemover {

    @Override
    public ImageProcessor removeLines(int[] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth()-1, imgProcessor.getHeight());
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth()-1, imgProcessor.getHeight());
        } else {
            throw new UnsupportedOperationException();
        }
        
        int shift;
        for (int y = 0; y < imgProcessor.getHeight(); y++) {
            shift = 0;
            for (int x = 0; x < imgProcessor.getWidth(); x++) {
                if (toRemove[y] == x) {
                    shift = 1;
                    continue;
                }
                
                newIp.putPixel(x-shift, y, imgProcessor.getPixel(x, y));
            }
        }
        
        return newIp;
    }

}
