
package khl.dip.assignment;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ij.ImagePlus;
import ij.gui.ImageWindow;
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
    
    // Params
    @Parameter(
            names = {"-i", "--input"}, 
            converter = ImagePlusConverter.class, 
            required = true,
            description = "Input image")
    private ImagePlus img;
    
    @Parameter(
            names = {"-v", "--vertical"},
            description = "Number of vertical lines to be removed or added."
            )
    private int linesToRemove = 0;
    
    @Parameter(
            names = {"-o", "--output"},
            description = "File to write the carved image to. (If not provided, image is displayed."
            )
    private String outFile;
    
    @Parameter(
            names = {"-h", "--horizontal"},
            description = "Number of horizontal lines to be removed or added."
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
        
        removeLines(linesToRemove, new VerticalLineRemover(), new CumulativeVerticalImportance());
        removeLines(horizontalLinesToRemove, new HorizontalLineRemover(), new CumulativeHorizontalImportance());
        
        img.setProcessor(imgProcessor);
        
        showOrSave();
    }
    
    private void removeLines(int linesToRemove, LineRemover lineRemover, CumulativeImportance cumulativeImportance) {
        while (linesToRemove > 0) {
            importance();
            cumulativeImportance(cumulativeImportance);
            int[] toRemove = minimalImportance(cumulativeImportance);
            this.imgProcessor = lineRemover.removeLines(toRemove, imgProcessor);
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
    private void cumulativeImportance(CumulativeImportance cumulativeImportance) {
        cumulativeImportance.applyTo(grayscale);
    }
    
    // Step 3: Select a Line with Minimal Importance
    private int[] minimalImportance(CumulativeImportance cumulativeImportance) {
        return cumulativeImportance.getLine(cumulativeImportance.getLeastImportantLine());
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
