package khl.dip.assignment;

import com.beust.jcommander.ParameterException;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
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
    private int[][] grayimg;
    private final Sobel sobel = new Sobel();
    private final CarveParams params;
    private static final Logger LOGGER = Logger.getLogger(Carve.class.toString());
    private final LineChanger lineChanger = new LineChanger();
    private final CumulativeImportance cumulImportance = new CumulativeImportance();

    public Carve(final CarveParams params) {
        this.params = params;
    }

    public void run() {
        this.imgProcessor = params.img.getProcessor();
        
        if (params.vertLinesToAlter > 0 || params.horiLinesToAlter > 0) {
            // Means we actually have to do something.
            this.grayimg = desaturate.applyTo(imgProcessor);
            
            alterLines(params.vertLinesToAlter);
            
            if (params.horiLinesToAlter > 0) {
                imgProcessor = switchXandY(imgProcessor);
                grayimg = switchXandY(grayimg);
                params.prioritizedPixels = switchXandY(params.prioritizedPixels);
                params.protectedPixels = switchXandY(params.protectedPixels);
                alterLines(params.horiLinesToAlter);
                grayimg = switchXandY(grayimg);
                imgProcessor = switchXandY(imgProcessor);
            }
        }

        params.img.setProcessor(imgProcessor);

        showOrSave();
    }

    private void alterLines(final int linesToAlter) {
        if (params.linesPerTime > 1) {
            batchAlterLines(linesToAlter);
        } else {
            for (int altered = 0; altered < linesToAlter; altered++) {
                execAlter();
            }
        }
    }

    private int execAlter(final int numLines) {
        int alteredLines;
        
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance, numLines);
        alteredLines = toChange.length;
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.markLines, params.prioritizedPixels, params.protectedPixels, grayimg);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
        grayimg = lineChanger.grayimg;
        
        return alteredLines;
    }

    private void execAlter() {
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.markLines, params.prioritizedPixels, params.protectedPixels, grayimg);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
        grayimg = lineChanger.grayimg;
    }

    private void batchAlterLines(final int linesToAlter) {
        int linesDone = 0;
        while (linesDone < linesToAlter) {
            linesDone += execAlter(Math.min(params.linesPerTime, linesToAlter-linesDone));
        }
    }

    public ImagePlus getImage() {
        return params.img;
    }

    // Step 1: Compute the Importance
    private void importance() {
        // Apply the Sobel operator to the grayscale copy to detect edges.
        this.grayscale = sobel.applyTo(grayimg);

        // Apply a 3x3 maximum filter to spread the influence of edges to nearby pixels. 
        this.grayscale = grayMax.applyTo(grayscale);
    }

    // Step 2: Compute the Cumulative Importance
    private void cumulativeImportance(final CumulativeImportance cumulImportance, final boolean[][] prioritized, final boolean[][] protectedPixels) {
        cumulImportance.applyTo(grayscale, prioritized, protectedPixels);
    }

    // Step 3: Select a Line with Minimal Importance
    private int[][] minimalImportance(final CumulativeImportance cumulImportance, final int count) {
        return cumulImportance.getLeastImportantLines(count);
    }

    private int[][] minimalImportance(final CumulativeImportance cumulImportance) {
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

    private int[][] switchXandY(final int[][] orig) {
        int[][] result = new int[orig[0].length][orig.length];
        
        for (int x = 0; x < orig.length; x++) {
            for (int y = 0; y < orig[0].length; y++) {
                result[y][x] = orig[x][y];
            }
        }
        
        return result;
    }
    
    private boolean[][] switchXandY(final boolean[][] orig) {
        boolean[][] result = new boolean[orig[0].length][orig.length];
        
        for (int x = 0; x < orig.length; x++) {
            for (int y = 0; y < orig[0].length; y++) {
                result[y][x] = orig[x][y];
            }
        }
        
        return result;
    }
    
    private ImageProcessor switchXandY(final ImageProcessor orig) {
        ImageProcessor result;
        
        if (orig instanceof ColorProcessor) {
            result = new ColorProcessor(orig.getHeight(), orig.getWidth());
        } else if (orig instanceof ByteProcessor) {
            result = new ByteProcessor(orig.getHeight(), orig.getWidth());
        } else {
            throw new UnsupportedOperationException();
        }
        
        for (int x = 0; x < orig.getWidth(); x++) {
            for (int y = 0; y < orig.getHeight(); y++) {
                result.putPixel(y, x, orig.getPixel(x, y));
            }
        }
        
        return result;
    }
}
