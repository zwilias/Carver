package khl.dip.assignment;

import java.util.ArrayList;
import java.util.List;

public class Point {

    private final int x, y;

    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#Simplification
    // Thank you, wikipedia.
    public List<Point> getPointsOnLineTo(final Point otherPoint) {
        final List<Point> result = new ArrayList<Point>();
        result.add(this);

        int x0 = this.getX();
        int y0 = this.getY();
        final int x1 = otherPoint.getX();
        final int y1 = otherPoint.getY();

        final int dx = Math.abs(x1 - x0);
        final int dy = Math.abs(y1 - y0);
        final int sx = (x0 < x1) ? 1 : -1;
        final int sy = (y0 < y1) ? 1 : -1;

        int err = dx - dy;

        while (!(x0 == x1 && y0 == y1)) {
            final int doubleErr = 2 * err;

            if (doubleErr > -dy) {
                err -= dy;
                x0 += sx;
            }

            if (doubleErr < dx) {
                err += dx;
                y0 += sy;
            }

            result.add(new Point(x0, y0));
        }

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result;
        
        if (obj instanceof Point) {
            final Point point = (Point) obj;
            if (this.x == point.getX() && this.y == point.getY()) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return x + "x" + y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        return hash;
    }
}
