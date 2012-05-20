package khl.dip.assignment;

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
    private final CarveParams params;

    public Carve(final CarveParams params) {
        this.params = params;
    }

    public void run() {
        this.imgProcessor = params.img.getProcessor();

        alterLines(params.vertLinesToAlter, new VerticalLineChanger(), new CumulativeVerticalImportance());
        alterLines(params.horiLinesToAlter, new HorizontalLineChanger(), new CumulativeHorizontalImportance());

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

    private void execAlter(final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger, final int numLines) {
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance, numLines);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.prioritizedPixels, params.protectedPixels);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
    }

    private void execAlter(final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger) {
        importance();
        cumulativeImportance(cumulImportance, params.prioritizedPixels, params.protectedPixels);
        final int[][] toChange = minimalImportance(cumulImportance);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.prioritizedPixels, params.protectedPixels);
        params.prioritizedPixels = lineChanger.prioritizedPixels;
        params.protectedPixels = lineChanger.protectedPixels;
    }

    private void batchAlterLines(final int linesToAlter, final AbstractCumulativeImportance cumulImportance, final AbstractLineChanger lineChanger) {
        int linesDone = 0;
        while (linesDone < linesToAlter - linesToAlter % params.linesPerTime) {
            execAlter(cumulImportance, lineChanger, params.linesPerTime);
            linesDone += params.linesPerTime;
        }

        // If we still have some lines left to handle, do it
        if (linesToAlter % params.linesPerTime > 0) {
            execAlter(cumulImportance, lineChanger, linesToAlter % params.linesPerTime);
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
