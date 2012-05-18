
package khl.dip.assignment;

import java.util.Collections;
import java.util.LinkedList;

public class CumulativeVerticalImportance extends CumulativeImportance {

    @Override
    protected void populateDirections(final int[][] pixels) {
        populateInitialImportance(pixels);
        
        // Run through the rest of the lines, figuring out their minimal
        // cumulative importance along the way.
        for (int y = 1; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                findImportanceAndDirection(pixels, x, y);
            }
        }
    }

    @Override
    protected void populateInitialImportance(final int[][] pixels) {
        // Copy the first line into the cumulativeImportance matrix
        for (int x = 0; x < this.width; x++) {
            importanceGrid[0][x] = pixels[x][0];
        }
    }
    
    @Override
    protected int findMinimalNeighbor(int x, int y, int direction) {
        return importanceGrid[y-1][x+direction];
    }

    @Override
    protected int getDirection(int x, int y) {
        int direction = 0;
        if (x == 0) {
            if (importanceGrid[y-1][x] > importanceGrid[y-1][x+1]) {
                direction = 1;
            }
        } else if (x == this.width-1) {
            if (importanceGrid[y-1][x-1] < importanceGrid[y-1][x]) {
                direction = -1;
            }
        } else {
            if (importanceGrid[y-1][x-1] < importanceGrid[y-1][x] && importanceGrid[y-1][x] <= importanceGrid[y-1][x+1]) {
                // left smallest
                direction = -1;
            } else if (importanceGrid[y-1][x-1] >= importanceGrid[y-1][x] && importanceGrid[y-1][x] > importanceGrid[y-1][x+1]) {
                // right smallest
                direction = 1;
            }
        }
        return direction;
    }
    
    @Override
    public int[] getCumulativeImportance() {
        return importanceGrid[this.height-1];
    }
    
    @Override
    public int[] getLine(int x) {
        final int[] result = new int[this.height];
        
        result[result.length-1] = x;
        
        for (int y = result.length-1; y > 0; y--) {
            x += directions[x][y];
            result[y-1] = x;
        }
        
        return result;
    }

    @Override
    public int[][] getLeastImportantLines(int count) {
        int[][] sorted = new int[count][this.height];
        int[][] usedMatrix = new int[this.width][this.height];
        int[] tmp;
        LinkedList<SortableKeyValuePair> cumuls = new LinkedList<SortableKeyValuePair>();
        
        // First get all the cumulative importance things
        for (int i = 0; i < this.width; i++) {
            cumuls.add(new SortableKeyValuePair(i, importanceGrid[this.height-1][i]));
        }
        
        Collections.sort(cumuls);
        
        tmp = getLine(cumuls.poll().getKey());
        for (int y = this.height -1; y >= 0; y--) {
            usedMatrix[tmp[y]][y] = 1;
        }
        
        int i = 1;
        
        while (i < count && cumuls.size() > 0) {
            tmp = getLine(cumuls.poll().getKey());
            
            // Find out if this line crosses any other line already in the array
            boolean conflict = false;
            for (int y = this.height-1; !conflict && y >= 0; y--) {
                conflict = usedMatrix[tmp[y]][y] == 1;
            }
            
            // If it does, head on to the next line
            if (conflict) {
                continue;
            }
            
            // otherwise, mark every cell this one uses as used
            for (int y = this.height -1; y >= 0; y--) {
                usedMatrix[tmp[y]][y] = 1;
            }
            
            i += 1;
        }
        
        // Almost there..
        for (int y = 0; y < height; y++) {
            int cnt = 0;
            for (int x = 0; x < width; x++) {
                if (usedMatrix[x][y] == 1) {
                    sorted[cnt++][y] = x;
                }
            }
        }
        
        return sorted;
    }
}
