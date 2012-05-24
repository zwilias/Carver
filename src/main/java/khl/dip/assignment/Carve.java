package khl.dip.assignment;

import com.beust.jcommander.ParameterException;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Carve {

    private final Desaturate desaturate = new Desaturate();
    private final Gray8Max grayMax = new Gray8Max();
    private ImageProcessor imgProcessor;
    private int[][] grayscale;
    private final Sobel sobel = new Sobel();
    private final CarveParams params;
    private static final Logger LOGGER = Logger.getLogger(Carve.class.toString());

    public Carve(final CarveParams params) {
        this.params = params;
    }

    public void run() {
        this.imgProcessor = params.img.getProcessor();
        
        LOGGER.log(Level.INFO, "Preparing to alter {0} vertical lines and {1} horizontal lines on a {2}x{3} image.", new Object[]{params.vertLinesToAlter, params.horiLinesToAlter, imgProcessor.getWidth(), imgProcessor.getHeight()});

        alterLines(params.vertLinesToAlter, new VerticalLineChanger(), new CumulativeVerticalImportance());
        
        LOGGER.log(Level.ALL, "Altered {0} vertical lines", params.vertLinesToAlter);
        
        alterLines(params.horiLinesToAlter, new HorizontalLineChanger(), new CumulativeHorizontalImportance());
        
        LOGGER.log(Level.ALL, "Altered {0} horizontal lines", params.horiLinesToAlter);

        params.img.setProcessor(imgProcessor);

        showOrSave();
    }

    private void alterLines(final int linesToAlter, final AbstractLineChanger lineChanger, final AbstractCumulativeImportance cumulImportance) {
        if (params.linesPerTime > 1) {
            batchAlterLines(linesToAlter, cumulImportance, lineChanger);
        } else {
            for (int altered = 0; altered < linesToAlter; altered++) {
                execAlter(cumulImportance, lineChanger);
            }
        }
    }

    private int execAlter(final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger, final int numLines) {
        int alteredLines;
        
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance, numLines);
        alteredLines = toChange.length;
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.markLines, params.prioritizedPixels, params.protectedPixels);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
        
        return alteredLines;
    }

    private void execAlter(final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger) {
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.markLines, params.prioritizedPixels, params.protectedPixels);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
    }

    private void batchAlterLines(final int linesToAlter, final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger) {
        int linesDone = 0;
        while (linesDone < linesToAlter) {
            linesDone += execAlter(cumulImportance, lineChanger, Math.min(params.linesPerTime, linesToAlter-linesDone));
        }
    }

    public ImagePlus getImage() {
        return params.img;
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
    private void cumulativeImportance(final AbstractCumulativeImportance cumulImportance, final boolean[][] prioritized, final boolean[][] protectedPixels) {
        cumulImportance.applyTo(grayscale, prioritized, protectedPixels);
    }

    // Step 3: Select a Line with Minimal Importance
    private int[][] minimalImportance(final AbstractCumulativeImportance cumulImportance, final int count) {
        return cumulImportance.getLeastImportantLines(count);
    }

    private int[][] minimalImportance(final AbstractCumulativeImportance cumulImportance) {
        return new int[][]{cumulImportance.getLine(cumulImportance.getLeastImportantLine())};
    }

    private void showOrSave() {
        if (params.facebookAccessToken != null) {
            try {
                final String shortname = params.img.getShortTitle();
                final FacebookClient fbClient = new DefaultFacebookClient(params.facebookAccessToken);
                final BufferedImage bImg = params.img.getBufferedImage();
                final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                ImageIO.write(bImg, "png", byteOutStream);
                final InputStream inputStream = new ByteArrayInputStream(byteOutStream.toByteArray());
                
                final FacebookType response = fbClient.publish("me/photos", 
                        FacebookType.class, 
                        BinaryAttachment.with(shortname, inputStream), 
                        Parameter.with("message", "Resized by Carver v1.0"));
                System.out.println("https://www.facebook.com/photo.php?fbid=" + response.getId());
            } catch (IOException ex) {
                Logger.getLogger(Carve.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        if (params.outFile == null) {
            final ImageWindow window = new ImageWindow(params.img);
            window.setVisible(true);
        } else {
            try {
                final String type = params.outFile.substring(params.outFile.lastIndexOf(".") + 1);
                ImageIO.write(params.img.getBufferedImage(), type, new File(params.outFile));
            } catch (IOException ex) {
                throw new ParameterException("Could not write to " + params.outFile + ": " + ex.getMessage());
            }
        }
    }
}
