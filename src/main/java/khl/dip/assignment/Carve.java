
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
    private int verticalLinesToAlter = 0;
    
    @Parameter(
            names = {"-o", "--output"},
            description = "File to write the carved image to. (If not provided, image is displayed."
            )
    private String outFile;
    
    @Parameter(
            names = {"-h", "--horizontal"},
            description = "Number of horizontal lines to be removed or added."
            )
    private int horizontalLinesToAlter = 0;
    
    @Parameter(
            names = {"--help"},
            description = "Show usage."
            )
    private boolean showUsage;
    
    @Parameter(
            names = {"-a", "--add-lines"},
            description = "When set, the image will be enlarged, not shrinked."
            )
    private boolean addLines = false;
    
    public boolean isShowUsage() {
        return this.showUsage;
    }
      
    public void run() {
        this.imgProcessor = img.getProcessor();
        
        alterLines(verticalLinesToAlter, new VerticalLineChanger(), new CumulativeVerticalImportance());
        alterLines(horizontalLinesToAlter, new HorizontalLineChanger(), new CumulativeHorizontalImportance());
        
        img.setProcessor(imgProcessor);
        
        showOrSave();
    }
    
    private void alterLines(int linesToAlter, LineChanger lineChanger, CumulativeImportance cumulativeImportance) {
        int linesPerTime = 30; // Let's limit the number of lines we do each iteration
        int linesDone = 0;
        while (linesDone < linesToAlter - linesToAlter%linesPerTime) {
            importance();
            cumulativeImportance(cumulativeImportance);
            int[][] toChange = minimalImportance(cumulativeImportance, linesPerTime);
            this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, addLines);
            linesDone += linesPerTime;
        }
        
        // If we still have some lines left to handle, do it
        if (linesToAlter%linesPerTime > 0) {
            importance();
            cumulativeImportance(cumulativeImportance);
            int[][] toChange = minimalImportance(cumulativeImportance, linesToAlter%linesPerTime);
            this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, addLines);
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
    private int[][] minimalImportance(CumulativeImportance cumulativeImportance, int count) {
        return cumulativeImportance.getLeastImportantLines(count);
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
