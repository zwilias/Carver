
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
    
    public List<Point> getPointsOnLineTo(Point otherPoint) {
        List<Point> result = new ArrayList<Point>();
        
        if (otherPoint == null) {
            result.add(this);
        }
        
        return result;
    }
}
