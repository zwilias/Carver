
package khl.dip.assignment;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;


public class Desaturate {
    /* In this case, we apply it on a duplicate and return it instead 
     * of applying it in-place. Keep this in mind.
     */
    public ByteProcessor applyTo(ImageProcessor ip) {
        ByteProcessor result;
        
        if (ip instanceof ColorProcessor) {
            result = new ByteProcessor(ip.getWidth(), ip.getHeight());
            
            for (int x = 0; x < ip.getWidth(); x++) {
                for (int y = 0; y < ip.getHeight(); y++) {
                    result.putPixel(x, y, desaturate(ip.getPixel(x, y)));
                }
            }
        } else if (ip instanceof ByteProcessor) {
            result = (ByteProcessor) ip.duplicate();
        } else {
            throw new UnsupportedOperationException();
        }
        
        return result;
    }

    public int desaturate(int color) {
        int[] parts = getRGBFromInt(color);
        return (int) (0.21 * parts[0] + 0.72 * parts[1] + 0.07 * parts[2]);
    }

    private int[] getRGBFromInt(int color) {
        int[] comps = new int[3];
        comps[0] = (color & 0xff0000) >> 16;	//r
        comps[1] = (color & 0x00ff00) >> 8;     //g
        comps[2] = (color & 0x0000ff);          //b
        return comps;
    }
}
