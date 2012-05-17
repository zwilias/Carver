
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
    private int[][] prioritizedPixels;
    private int[][] protectedPixels;
    
    // Params
    @Parameter(
            names = {"-i", "--input"}, 
            converter = ImagePlusConverter.class, 
            required = true,
            description = "Input image")
    private ImagePlus img;
    
    @Parameter(
            names = {"-v", "--vertical"},
            description = "Number of vertical lines to be removed."
            )
    private int linesToRemove = 0;
    
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
    
    @Parameter(
            names = {"--help"},
            description = "Show usage."
            )
    private boolean showUsage;
    
    public boolean isShowUsage() {
        return this.showUsage;
    }
      
    public void run() {
        this.imgProcessor = img.getProcessor();
        
        removeVerticalLines();
        removeHorizontalLines();
        
        img.setProcessor(imgProcessor);
        
        showOrSave();
    }

    private void removeHorizontalLines() {
        while (horizontalLinesToRemove > 0) {
            importance();
            cumulativeHorizontalImportance();
            minimalHorizontalImportance();
            removeLeastHorizontalImportant();
            horizontalLinesToRemove--;
        }
    }

    private void removeVerticalLines() {
        while (linesToRemove > 0) {
            importance();
            cumulativeImportance();
            minimalImportance();
            removeLeastImportant();
            linesToRemove--;
        }
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
    
    // Step 2b: Compute the Cumulative Horizontal Importance
    private void cumulativeHorizontalImportance() {
        chi = new CumulativeHorizontalImportance(grayscale);
    }
    
    // Step 3: Select a Line with Minimal Importance
    private void minimalImportance() {
        this.toRemove = cvi.getLine(cvi.getLeastImportantLine());
    }
    
    // Step 3b: Select a horizontal Line with Minimal Importance
    private void minimalHorizontalImportance() {
        this.toRemove = chi.getLine(chi.getLeastImportantLine());
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
    
    // Step 4b: Remove that line
    private void removeLeastHorizontalImportant() {
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
