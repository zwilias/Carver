package khl.dip.assignment;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ij.ImagePlus;

public class CarveParams {

    @Parameter(names = {"-i", "--input"},
               converter = ImagePlusConverter.class,
               required = true,
               description = "Input image")
    public ImagePlus img;
    @Parameter(names = {"-v", "--vertical"},
               description = "Number of vertical lines to be removed or added.")
    public int verticalLinesToAlter = 0;
    @Parameter(names = {"-o", "--output"},
               description = "File to write the carved image to. (If not provided, image is displayed.")
    public String outFile;
    @Parameter(names = {"-h", "--horizontal"},
               description = "Number of horizontal lines to be removed or added.")
    public int horizontalLinesToAlter = 0;
    @Parameter(names = {"--help"},
               description = "Show usage.")
    public boolean showUsage;
    @Parameter(names = {"-a", "--add-lines"},
               description = "When set, the image will be enlarged, not shrinked.")
    public boolean addLines = false;
    @Parameter(names = {"-c", "--lines-per-batch"},
               description = "How many lines will be removed in each batched action.\nSetting this to 1 will bypass batching.")
    public int linesPerTime = 30;

    public void checkParams() {
        if (!addLines && (verticalLinesToAlter >= img.getWidth() || horizontalLinesToAlter >= img.getHeight())) {
            throw new ParameterException("Can't make image that small, there won't be anything left.");
        }

        if (verticalLinesToAlter < 0 || horizontalLinesToAlter < 0) {
            throw new ParameterException("Can't alter a negative number of lines.");
        }
    }
}
