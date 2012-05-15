
package khl.dip.assignment;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Carve {
    private final Desaturate desaturate = new Desaturate();
    private final Gray8Max grayMax = new Gray8Max();
    private ImageProcessor imgProcessor;
    private int[][] grayscale;
    private final Sobel sobel = new Sobel();
    private int[] toRemove;
    private CumulativeVerticalImportance cvi;
    private CumulativeHorizontalImportance chi;
    
    // Params
    @Parameter(
            names = {"-i", "--input"}, 
            converter = ImagePlusConverter.class, 
            required = true,
            description = "Input image")
    private ImagePlus img;
    
    @Parameter(
            names = {"-l", "--lines"},
            description = "Number of vertical lines to be removed."
            )
    private int linesToRemove = 200;
    
    @Parameter(
            names = {"-o", "--output"},
            description = "File to write the carved image to. (If not provided, image is displayed."
            )
    private String outFile;
    
    @Parameter(
            names = {"-h", "--horizontal"},
            description = "Number of horizontal lines to be removed."
            )
    private int horizontalLinesToRemove = 0;
    
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
    
    public void run() {
        this.imgProcessor = img.getProcessor();
        
        while (linesToRemove > 0) {
            importance();
            cumulativeImportance();
            minimalImportance();
            removeLeastImportant();
            linesToRemove--;
        }
        
        img.setProcessor(imgProcessor);
        
        showOrSave();
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
        cvi = new CumulativeVerticalImportance(grayscale);
    }
    
    // Step 3: Select a Line with Minimal Importance
    private void minimalImportance() {
        this.toRemove = cvi.getLine(cvi.getLeastImportantLine());
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
        imgProcessor = newIp;
    }

    private void showOrSave() {
        if (outFile == null) {
            ImageWindow window = new ImageWindow(img);
            window.setVisible(true);
        } else {
            try {
                String type = outFile.substring(outFile.lastIndexOf(".") + 1);
                ImageIO.write(img.getBufferedImage(), type, new File(outFile));
            } catch (IOException ex) {
                throw new ParameterException("Could not write to " + outFile + ": " + ex.getMessage());
            }
        }
    }
}
