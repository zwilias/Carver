
package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class HorizontalLineRemover implements LineRemover {

    @Override
    public ImageProcessor removeLines(int[] toRemove, ImageProcessor imgProcessor) {
        ImageProcessor newIp;
        
        if (imgProcessor instanceof ColorProcessor) {
            newIp = new ColorProcessor(imgProcessor.getWidth(), imgProcessor.getHeight()-1);
        } else if (imgProcessor instanceof ByteProcessor) {
            newIp = new ByteProcessor(imgProcessor.getWidth(), imgProcessor.getHeight()-1);
        } else {
            throw new UnsupportedOperationException();
        }
        
        int shift;
        for (int x = 0; x < imgProcessor.getWidth(); x++) {
            shift = 0;
            for (int y = 0; y < imgProcessor.getHeight(); y++) {
                if (toRemove[x] == y) {
                    shift = 1;
                    continue;
                }
                
                newIp.putPixel(x, y-shift, imgProcessor.getPixel(x, y));
            }
        }
        
        return newIp;
    }

}
