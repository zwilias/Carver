
package khl.dip.assignment;

import com.beust.jcommander.ParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ShapeCreator {
    public boolean[][] createPixelMatrix(final List<Point> corners, final Point shapePoint, boolean[][] pixelMatrix) {
        final int width = pixelMatrix.length;
        final int height = pixelMatrix[0].length;
        pixelMatrix = markEdges(corners, createShape(width, height));
        fillShape(pixelMatrix, shapePoint);
        return pixelMatrix;
    }

    public boolean[][] markEdges(final List<Point> corners, boolean[][] pixelMatrix) {
        Point previous = corners.get(corners.size() - 1);
        for (Point current : corners) {
            for (Point p : current.getPointsOnLineTo(previous)) {
                pixelMatrix[p.getX()][p.getY()] = true;
            }
            previous = current;
        }

        return pixelMatrix;
    }

    public boolean[][] createPixelMatrix(final List<Point> cornerList, final Point shapePoint, final int width, final int height) {
        boolean[][] pixelMatrix;
        if (cornerList.isEmpty()) {
            pixelMatrix = createShape(width, height);
        } else {
            checkCorners(cornerList, width, height);
            try {
                checkPoint(shapePoint, width, height);
            } catch (ParameterException e) {
                throw new ParameterException("The supplied point inside the shape was not valid.");
            }
            pixelMatrix = createPixelMatrix(cornerList, shapePoint, createShape(width, height));
        }
        return pixelMatrix;
    }

    public void checkCorners(final List<Point> cornerList, final int width, final int height) {
        for (Point p : cornerList) {
            checkPoint(p, width, height);
        }
    }
    
    public void checkPoint(final Point p, final int width, final int height) {
        if (p == null) {
            throw new ParameterException("No point.");
        }
        
        if (p.getX() < 0 || p.getX() >= width || p.getY() < 0 || p.getY() >= height) {
            throw new ParameterException("Coordinate '" + p + "' refers to a point outside the image.");
        }
    }
    
    public void fillShape(boolean[][] pixelMatrix, final Point shapePoint) {
        final Queue<Point> queue = new LinkedList<Point>();
        final int width = pixelMatrix.length;
        final int height = pixelMatrix[0].length;
        queue.add(shapePoint);
        
        while (!queue.isEmpty()) {
            final Point p = queue.poll();
            if (p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height && !pixelMatrix[p.getX()][p.getY()]) {
                int w = p.getX();
                int e = p.getX();
                
                for (; w > 0 && !pixelMatrix[w][p.getY()]; w--) {}
                for (; e < width && !pixelMatrix[e][p.getY()]; e++) {}
                
                for (int x = w+1; x < e; x++) {
                    pixelMatrix[x][p.getY()] = true;
                                    
                    if (p.getY() > 0 && !pixelMatrix[x][p.getY() - 1]) { queue.add(new Point(x, p.getY() - 1)); }
                    if (p.getY()+1 < height && !pixelMatrix[x][p.getY() + 1]) { queue.add(new Point(x, p.getY() + 1)); }
                }
            }
        }
    }

    public boolean[][] createShape(final int width, final int height) {
        return new boolean[width][height];
    }
}
