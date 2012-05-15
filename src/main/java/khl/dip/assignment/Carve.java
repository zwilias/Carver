
package khl.dip.assignment;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Carve {
    private final Desaturate desaturate = new Desaturate();
    private final Gray8Max grayMax = new Gray8Max();
    private ImageProcessor imgProcessor;
    private int[][] grayscale;
    private final Sobel sobel = new Sobel();
    private int[] toRemove;
    private CumulativeImportance ci;
    private ImagePlus img;
    
    public Carve(ImagePlus img) {
        this.imgProcessor = img.getProcessor();
        this.img = img;
    }
    
    public void benchmark(int iterations) {
        long startTime, endTime, diff;
        
        System.out.println("Starting benchmark. " + iterations + " iterations");
        
        /// IMPORTANCE ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            importance();
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Importance:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");
    
        /// CUMULATIVE ///      
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            cumulativeImportance();
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Cumulative:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");
        
        /// MINIMAL ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            minimalImportance();
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Minimal:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");
         
        /// REMOVAL ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            removeLeastImportant();
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Removal:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");
 
    }
    
    public void benchmarkImportance(int iterations) {
        long startTime, endTime, diff;
        
        System.out.println(iterations + " iterations");
        
        /// DESATURATE ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            desaturate.applyTo(imgProcessor);
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Desaturate:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");

        this.grayscale = desaturate.applyTo(imgProcessor);
        
        /// Sobel ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            sobel.applyTo(grayscale);
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Sobel:\t\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");

        /// Graymax ///
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            grayMax.applyTo(grayscale);
        }
        endTime = System.currentTimeMillis();
        diff = endTime - startTime;
        
        System.out.println("Graymax:\t" + diff + "ms\t" + iterations + " iterations\t" + ((double)diff/iterations) + " average");

    }
    
    public Carve(ImagePlus img, int linesToRemove) {
        this.img = img;
        this.imgProcessor = img.getProcessor();
        
        while (linesToRemove > 0) {
            importance();
            cumulativeImportance();
            minimalImportance();
            removeLeastImportant();
            linesToRemove--;
        }
        
        this.img.setProcessor(imgProcessor);
    }
    
    public ImagePlus getImage() {
        return this.img;
    }
    
    // Step 1: Compute the Importance
    private void importance() {        
        // Create a grayscale copy of the current image.
        this.grayscale = desaturate.applyTo(imgProcessor);
        
        // Apply the Sobel operator to the grayscale copy to detect edges.
        this.grayscale = sobel.applyTo(grayscale);
        
        // Apply a 3x3 maximum filter to spread the influence of edges to nearby pixels. 
        this.grayscale = grayMax.applyTo(grayscale);
    }
    
    // Step 2: Compute the Cumulative Importance
    private void cumulativeImportance() {
        ci = new CumulativeImportance(grayscale);
    }
    
    // Step 3: Select a Line with Minimal Importance
    private void minimalImportance() {
        this.toRemove = ci.getLine(ci.getLeastImportantLine());
    }
    
    // Step 4: Remove that line
    private void removeLeastImportant() {
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
        
        // TODO: Don't forget to uncomment this line
        // it doesn't actually work without it, but it ruins the benching
        //imgProcessor = newIp;
    }
}
