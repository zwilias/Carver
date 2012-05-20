package khl.dip.assignment;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.validators.PositiveInteger;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.List;

public class CarveParams {

    @Parameter(names = {"-i", "--input"},
               converter = ImagePlusConverter.class,
               required = true,
               description = "Input image")
    public ImagePlus img;
    
    @Parameter(names = {"-v", "--vertical"},
               description = "Number of vertical lines to be removed or added.",
               validateWith = PositiveInteger.class)
    public int vertLinesToAlter = 0;
    
    @Parameter(names = {"-o", "--output"},
               description = "File to write the carved image to. (If not provided, image is displayed.")
    public String outFile;
    
    @Parameter(names = {"-h", "--horizontal"},
               description = "Number of horizontal lines to be removed or added.",
               validateWith = PositiveInteger.class)
    public int horiLinesToAlter = 0;
    
    @Parameter(names = {"--help"},
               description = "Show usage.")
    public boolean showUsage;
    
    @Parameter(names = {"-a", "--add-lines"},
               description = "When set, the image will be enlarged, not shrinked.")
    public boolean addLines = false;
    
    @Parameter(names = {"-c", "--lines-per-batch"},
               description = "How many lines will be removed in each batched action. Setting this to 1 will bypass batching.")
    public int linesPerTime = 30;
    
    @Parameter(names = {"-p", "--prioritize"},
               description = "Comma-separated list of pixels to prioritize. Each pixel is in the format of XxY, with X its x-coordinate, 0-indexed on the left and Y its y-coordinate, 0-indexed on the top. These will be processed in the order in which they're supplied to create a shape with the supplied pixels functioning as corner-points.",
               splitter = CommaParameterSplitter.class,
               converter = PointConverter.class)
    private final List<Point> prioCorners = new ArrayList<Point>();
    
    @Parameter(names = {"-pi", "--prioritizedPoint"},
               description = "A point that lies inside the shape described by the parameters to -p/--prioritize. Used to fill the shape. Mandatory when using -p/--priorize.",
               converter = PointConverter.class)
    private Point prioritizedPoint;
    
    @Parameter(names = {"-s", "--protect"},
               description = "Comma-separated list of pixels to protect. Each pixel is in the format of XxY, with X its x-coordinate, 0-indexed on the left and Y its y-coordinate, 0-indexed on the top. These will be processed in the order in which they're supplied to create a shape with the supplied pixels functioning as corner-points.",
               splitter = CommaParameterSplitter.class,
               converter = PointConverter.class)
    private final List<Point> protCorners = new ArrayList<Point>();
    
    @Parameter(names = {"-si", "--protectedPoint"},
               description = "A point that lies inside the shape described by the parameters to -s/--protect. Used to fill the shape. Mandatory when using -s/--protect.",
               converter = PointConverter.class)
    private Point protectedPoint;
    
    public boolean[][] prioritizedPixels;
    public boolean[][] protectedPixels;

    public void checkParams() {
        if (!addLines && (vertLinesToAlter >= img.getWidth() || horiLinesToAlter >= img.getHeight())) {
            throw new ParameterException("Can't make image that small, there won't be anything left.");
        }

        final ShapeCreator creator = new ShapeCreator();
        prioritizedPixels = creator.createPixelMatrix(prioCorners, prioritizedPoint, img.getWidth(), img.getHeight());
        protectedPixels = creator.createPixelMatrix(protCorners, protectedPoint, img.getWidth(), img.getHeight());
    }
}
