
package khl.dip.assignment;

import com.beust.jcommander.ParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ShapeCreator {
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

    public boolean[][] createPixelMatrix(List<Point> cornerList, Point shapePoint, int width, int height) {
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
    
    public void fillShape(boolean[][] pixelMatrix, Point shapePoint) {
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
        ShapeCreator creator = new ShapeCreator();
        List<Point> corners = new ArrayList<Point>();

        corners.add(new Point(1, 1));
        corners.add(new Point(3, 0));
        corners.add(new Point(8, 1));
        corners.add(new Point(8, 8));
        corners.add(new Point(3, 7));
        corners.add(new Point(1, 9));
        
        Point shapePoint = new Point(5, 5);

        boolean[][] m = creator.createPixelMatrix(corners, shapePoint, creator.createShape(10, 10));

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                System.out.print(m[x][y] ? 1 : 0);
            }
            System.out.print("\n");
        }
    }
}
