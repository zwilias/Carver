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

    public Carve(CarveParams params) {
        this.params = params;
    }

    public void run() {
        this.imgProcessor = params.img.getProcessor();

        alterLines(params.verticalLinesToAlter, new VerticalLineChanger(), new CumulativeVerticalImportance());
        alterLines(params.horizontalLinesToAlter, new HorizontalLineChanger(), new CumulativeHorizontalImportance());

        params.img.setProcessor(imgProcessor);

        showOrSave();
    }

    private void alterLines(int linesToAlter, LineChanger lineChanger, CumulativeImportance cumulativeImportance) {
        if (params.linesPerTime > 1) {
            batchAlterLines(linesToAlter, cumulativeImportance, lineChanger);
        } else {
            while (linesToAlter > 0) {
                execAlter(cumulativeImportance, lineChanger);
                linesToAlter--;
            }
        }
    }

    private void execAlter(CumulativeImportance cumulativeImportance, LineChanger lineChanger, int numLines) {
        importance();
        cumulativeImportance(cumulativeImportance, params.prioritized);
        int[][] toChange = minimalImportance(cumulativeImportance, numLines);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.prioritized);
        params.prioritized = lineChanger.prioritizedPixels;
    }

    private void execAlter(CumulativeImportance cumulativeImportance, LineChanger lineChanger) {
        importance();
        cumulativeImportance(cumulativeImportance, params.prioritized);
        int[][] toChange = minimalImportance(cumulativeImportance);
        this.imgProcessor = lineChanger.changeLine(toChange, imgProcessor, params.addLines, params.prioritized);
        params.prioritized = lineChanger.prioritizedPixels;
    }

    private void batchAlterLines(int linesToAlter, CumulativeImportance cumulativeImportance, LineChanger lineChanger) {
        int linesDone = 0;
        while (linesDone < linesToAlter - linesToAlter % params.linesPerTime) {
            execAlter(cumulativeImportance, lineChanger, params.linesPerTime);
            linesDone += params.linesPerTime;
        }

        // If we still have some lines left to handle, do it
        if (linesToAlter % params.linesPerTime > 0) {
            execAlter(cumulativeImportance, lineChanger, linesToAlter % params.linesPerTime);
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
    private void cumulativeImportance(CumulativeImportance cumulativeImportance, boolean[][] prioritized) {
        cumulativeImportance.applyTo(grayscale, prioritized);
    }

    // Step 3: Select a Line with Minimal Importance
    private int[][] minimalImportance(CumulativeImportance cumulativeImportance, int count) {
        return cumulativeImportance.getLeastImportantLines(count);
    }

    private int[][] minimalImportance(CumulativeImportance cumulativeImportance) {
        return new int[][]{cumulativeImportance.getLine(cumulativeImportance.getLeastImportantLine())};
    }

    private void showOrSave() {
        if (params.outFile == null) {
            ImageWindow window = new ImageWindow(params.img);
            window.setVisible(true);
        } else {
            try {
                String type = params.outFile.substring(params.outFile.lastIndexOf(".") + 1);
                ImageIO.write(params.img.getBufferedImage(), type, new File(params.outFile));
            } catch (IOException ex) {
                throw new ParameterException("Could not write to " + params.outFile + ": " + ex.getMessage());
            }
        }
    }
}
