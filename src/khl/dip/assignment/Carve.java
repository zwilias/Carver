
package khl.dip.assignment;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Carve {
    private ImageProcessor imgProcessor;
    private ByteProcessor grayscale;
    private int[] toRemove;
    private CumulativeImportance ci;
    private ImagePlus img;
    
    public Carve(ImagePlus img, int linesToRemove) {
        this.img = img;
        this.imgProcessor = img.getProcessor();
        
        while (linesToRemove > 0) {
            System.out.print("Importance, ");
            importance();
            System.out.print("Cumulative, ");
            cumulativeImportance();
            System.out.print("Minimal, ");
            minimalImportance();
            System.out.print("Removing..");
            removeLeastImportant();
            System.out.println(" left: " + linesToRemove);
            linesToRemove--;
        }
        
        this.img.setProcessor(imgProcessor);
    }
    
    public ImagePlus getImage() {
        return this.img;
    }
    
    // Step 1: Compute the Importance
    private void importance() {
        // Prepare tools, filters
        Desaturate desaturate = new Desaturate();
        Sobel sobel = new Sobel();
        Gray8Max grayMax = new Gray8Max();
        
        // Create a grayscale copy of the current image.
        this.grayscale = desaturate.applyTo(imgProcessor);
        
        // Apply the Sobel operator to the grayscale copy to detect edges.
        sobel.applyTo(grayscale);
        
        // Apply a 3x3 maximum filter to spread the influence of edges to nearby pixels. 
        grayMax.applyTo(grayscale);
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
        
        imgProcessor = newIp;
    }
    
    public static void main(String[] args) {
        ImagePlus img = new ImagePlus("tower.png");
        
        Carve carve = new Carve(img, 200);
        img = carve.getImage();
        
        ImageWindow window = new ImageWindow(img);
        window.setVisible(true);
    }
}
