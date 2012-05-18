
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
    public List<Point> getPointsOnLineTo(Point otherPoint) {
        List<Point> result = new ArrayList<Point>();
        result.add(this);
        
        int x0 = this.getX();
        int y0 = this.getY();
        int x1 = otherPoint.getX();
        int y1 = otherPoint.getY();
        
        int dx = Math.abs(x1-x0);
        int dy = Math.abs(y1-y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        
        int err = dx - dy;
        
        while (!(x0 == x1 && y0 == y1)) {
            int doubleErr = 2*err;
            
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Point other = (Point) obj;
        if (this.x != other.getX()) {
            return false;
        }
        if (this.y != other.getY()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        return hash;
    }
    
    public static void main(String[] args) {
        for (Point p : new Point(0, 0).getPointsOnLineTo(new Point(0, 1))) {
            System.out.println(p.toString());
        }
    }
    
    public String toString() {
        return x + "x" + y;
    }
}
