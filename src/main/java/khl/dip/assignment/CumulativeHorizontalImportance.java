
package khl.dip.assignment;

public class CumulativeHorizontalImportance extends CumulativeImportance {
    
    public CumulativeHorizontalImportance(final int[][] pixels) {
        super(pixels);
    }

    @Override
    protected void populateDirections(final int[][] pixels) {
        populateInitialImportance(pixels);
        
        // Run through the rest of the lines, figuring out their minimal
        // cumulative importance along the way.
        for (int x = 1; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                findImportanceAndDirection(pixels, x, y);
            }
        }
    }

    @Override
    protected void populateInitialImportance(final int[][] pixels) {
        // Copy the first line into the cumulativeImportance matrix
        for (int y = 0; y < this.height; y++) {
            importanceGrid[y][0] = pixels[0][y];
        }
    }
    
    @Override
    protected int findMinimalNeighbor(int x, int y, int direction) {
        return importanceGrid[y+direction][x-1];
    }

    @Override
    protected int getDirection(int x, int y) {
        int direction = 0;
        if (y == 0) {
            if (importanceGrid[y][x-1] > importanceGrid[y+1][x-1]) {
                direction = 1;
            }
        } else if (y == this.height-1) {
            if (importanceGrid[y-1][x-1] < importanceGrid[y][x-1]) {
                direction = -1;
            }
        } else {
            if (importanceGrid[y-1][x-1] < importanceGrid[y][x-1] && importanceGrid[y][x-1] <= importanceGrid[y+1][x-1]) {
                // up smallest
                direction = -1;
            } else if (importanceGrid[y-1][x-1] >= importanceGrid[y][x-1] && importanceGrid[y][x-1] > importanceGrid[y+1][x-1]) {
                // down smallest
                direction = 1;
            }
        }
        return direction;
    }
    
    @Override
    public int[] getCumulativeImportance() {
        int[] result = new int[this.width];
        for (int i = 0; i < this.height; i++) {
            result[i] = importanceGrid[i][this.width-1];
        }
        return result;
    }
    
    @Override
    public int[] getLine(int idx) {
        final int[] result = new int[this.width];
        
        result[result.length-1] = idx;
        
        for (int x = result.length-1; x > 0; x--) {
            idx += directions[x][idx];
            result[x-1] = idx;
        }
        
        return result;
    }
}
