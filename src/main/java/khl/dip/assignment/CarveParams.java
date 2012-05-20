package khl.dip.assignment;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.validators.PositiveInteger;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
               splitter = CommaParameterSplitter.class,
               converter = PointConverter.class)
    private List<Point> prioritizedCorners = new ArrayList<Point>();
    
    @Parameter(names = {"-pi", "--prioritizedPoint"},
               description = "A point that lies inside the shape described by the parameters to -p/--prioritize. Used to fill the shape. Mandatory when using -p/--priorize.",
               converter = PointConverter.class)
    private Point prioritizedPoint;
    
    public boolean[][] prioritized;

    public void checkParams() {
        if (!addLines && (verticalLinesToAlter >= img.getWidth() || horizontalLinesToAlter >= img.getHeight())) {
            throw new ParameterException("Can't make image that small, there won't be anything left.");
        }

        prioritized = createPixelMatrix(prioritizedCorners, prioritizedPoint, img.getWidth(), img.getHeight());
    }

    public boolean[][] createPixelMatrix(List<Point> corners, Point shapePoint, boolean[][] pixelMatrix) {
        int width = pixelMatrix.length;
        int height = pixelMatrix[0].length;
        pixelMatrix = markEdges(corners, createShape(width, height));
        fillShape(pixelMatrix, shapePoint);
        return pixelMatrix;
    }

    public boolean[][] markEdges(List<Point> corners, boolean[][] pixelMatrix) {
        Point previous = corners.get(corners.size() - 1);
        for (Point current : corners) {
            for (Point p : current.getPointsOnLineTo(previous)) {
                pixelMatrix[p.getX()][p.getY()] = true;
            }
            previous = current;
        }

        return pixelMatrix;
    }

    private boolean[][] createPixelMatrix(List<Point> cornerList, Point shapePoint, int width, int height) {
        boolean[][] pixelMatrix;
        if (cornerList.size() > 0) {
            checkCorners(cornerList, width, height);
            try {
                checkPoint(shapePoint, width, height);
            } catch (ParameterException e) {
                throw new ParameterException("The supplied point inside the shape was not valid.");
            }
            pixelMatrix = createPixelMatrix(cornerList, shapePoint, createShape(width, height));
        } else {
            pixelMatrix = createShape(width, height);
        }
        return pixelMatrix;
    }

    public void checkCorners(List<Point> cornerList, int width, int height) {
        for (Point p : cornerList) {
            checkPoint(p, width, height);
        }
    }
    
    public void checkPoint(Point p, int width, int height) {
        if (p == null) {
            throw new ParameterException("No point.");
        }
        
        if (p.getX() < 0 || p.getX() >= width || p.getY() < 0 || p.getY() >= height) {
            throw new ParameterException("Coordinate '" + p + "' refers to a point outside the image.");
        }
    }
    
    private void fillShape(boolean[][] pixelMatrix, Point shapePoint) {
        Queue<Point> queue = new LinkedList<Point>();
        int width = pixelMatrix.length;
        int height = pixelMatrix[0].length;
        queue.add(shapePoint);
        
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height && !pixelMatrix[p.getX()][p.getY()]) {
                int w = p.getX();
                int e = p.getX();
                
                for (; w > 0 && !pixelMatrix[w][p.getY()]; w--) {}
                for (; e < width && !pixelMatrix[e][p.getY()]; e++) {}
                
                for (int x = w+1; x < e; x++) {
                    pixelMatrix[x][p.getY()] = true;
                                    
                    if (p.getY() > 0 && !pixelMatrix[x][p.getY() - 1]) queue.add(new Point(x, p.getY() - 1));
                    if (p.getY()+1 < height && !pixelMatrix[x][p.getY() + 1]) queue.add(new Point(x, p.getY() + 1));
                }
            }
        }
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
        
        Point shapePoint = new Point(5, 5);

        boolean[][] m = cv.createPixelMatrix(corners, shapePoint, cv.createShape(10, 10));

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                System.out.print(m[x][y] ? 1 : 0);
            }
            System.out.print("\n");
        }
    }
}
