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
    
    public boolean[][] prioritized;

    public void checkParams() {
        if (!addLines && (verticalLinesToAlter >= img.getWidth() || horizontalLinesToAlter >= img.getHeight())) {
            throw new ParameterException("Can't make image that small, there won't be anything left.");
        }

        prioritized = createPixelMatrix(prioritizedList, img.getWidth(), img.getHeight());
    }

    public boolean[][] createPixelMatrix(List<Point> corners, boolean[][] pixelMatrix) {
        int width = pixelMatrix.length;
        int height = pixelMatrix[0].length;
        pixelMatrix = markEdges(corners, createShape(width, height));
        pixelMatrix = fillShapes(pixelMatrix, corners);
        return pixelMatrix;
    }

    public boolean[][] markEdges(List<Point> corners, boolean[][] pixelMatrix) {
        Point previous = corners.get(corners.size()-1);
        for (Point current : corners) {
            for (Point p : current.getPointsOnLineTo(previous)) {
                pixelMatrix[p.getX()][p.getY()] = true;
            }
            previous = current;
        }
        
        return pixelMatrix;
    }

    private boolean[][] createPixelMatrix(List<String> coordinateList, int width, int height) {
        boolean[][] pixelMatrix;
        if (coordinateList.size() > 0) {
            ArrayList<Point> corners = parseCorners(coordinateList, width, height);
            pixelMatrix = createPixelMatrix(corners, createShape(width, height));
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
    
    // TODO: change this to first find a pixel that's IN the shape, then work from there
    // right now it's impossible to determine whether we're actually in the shape, there'
    // too many border-cases (got it?)
    public boolean[][] fillShapes(boolean[][] pixelMatrix, List<Point> corners) {
        boolean fillMode;
        for (int y = 0; y < pixelMatrix[0].length; y++) {
            fillMode = false;
            for (int x = 0; x < pixelMatrix.length; x++) {
                if (pixelMatrix[x][y]) {
                    if (!corners.contains(new Point(x, y))) {
                        if (x > 0 && pixelMatrix[x-1][y] && x+1 < pixelMatrix.length && pixelMatrix[x+1][y]) {
                            fillMode = false;
                        } else {
                            fillMode = !fillMode;
                        }
                    }
                } else {
                    pixelMatrix[x][y] = fillMode;
                }
            }
        }
        
        return pixelMatrix;
    }
    
    public boolean[][] createShape(int width, int height) {
        return new boolean[width][height];
    }
    
    public static void main(String[] args) {
        CarveParams cv = new CarveParams();
        List<Point> corners = new ArrayList<Point>();
        
        corners.add(new Point(1, 1));
        corners.add(new Point(3, 0));
        corners.add(new Point(8, 1));
        corners.add(new Point(8, 8));
        corners.add(new Point(3, 7));
        corners.add(new Point(1, 9));
        
        boolean[][] m = cv.createPixelMatrix(corners, cv.createShape(10, 10));
        
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                System.out.print(m[x][y] ? 1 : 0);
            }
            System.out.print("\n");
        }
    }
}
