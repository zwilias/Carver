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
    public int verticalLinesToAlter = 0;
    
    @Parameter(names = {"-o", "--output"},
               description = "File to write the carved image to. (If not provided, image is displayed.")
    public String outFile;
    
    @Parameter(names = {"-h", "--horizontal"},
               description = "Number of horizontal lines to be removed or added.",
               validateWith = PositiveInteger.class)
    public int horizontalLinesToAlter = 0;
    
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
               splitter = CommaParameterSplitter.class)
    private List<String> prioritizedList = new ArrayList<String>();
    
    public int[][] prioritized;

    public void checkParams() {
        if (!addLines && (verticalLinesToAlter >= img.getWidth() || horizontalLinesToAlter >= img.getHeight())) {
            throw new ParameterException("Can't make image that small, there won't be anything left.");
        }

        prioritized = createPixelMatrix(prioritizedList, img.getWidth(), img.getHeight());
    }

    public int[][] markEdges(List<Point> corners, int[][] pixelMatrix) {
        Point previous = corners.get(corners.size()-1);
        for (Point current : corners) {
            for (Point p : current.getPointsOnLineTo(previous)) {
                pixelMatrix[p.getX()][p.getY()] = 1;
            }
            previous = current;
        }
        
        return pixelMatrix;
    }

    private int[][] createPixelMatrix(List<String> coordinateList, int width, int height) {
        int[][] pixelMatrix;
        if (coordinateList.size() > 0) {
            ArrayList<Point> corners = parseCorners(coordinateList, width, height);
            pixelMatrix = markEdges(corners, createShape(width, height));
        } else {
            pixelMatrix = createShape(width, height);
        }
        return pixelMatrix;
    }

    public ArrayList<Point> parseCorners(List<String> coordinateList, int width, int height) throws ParameterException {
        ArrayList<Point> corners = new ArrayList<Point>(coordinateList.size());
        for (String param : coordinateList) {
            String[] coordinates = param.split("x");
            if (coordinates.length != 2) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }
            
            try {
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                
                if (x > width || y > height) {
                    throw new ParameterException("Coordinate '" + param + "' points to a pixel outside the current image");
                }

                corners.add(new Point(x, y));
            } catch (NumberFormatException e) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }
        }
        return corners;
    }

    public int[][] createShape(List<Point> corners, int width, int height) {
        int[][] pixelMatrix = createShape(width, height);
        
        markEdges(corners, pixelMatrix);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(pixelMatrix[x][y]);
            }
            System.out.print("\n");
        }
        
        return pixelMatrix;
    }
    
    public int[][] createShape(int width, int height) {
        return new int[width][height];
    }
}
