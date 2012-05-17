
package khl.dip.assignment;

import java.util.LinkedList;
import java.util.List;

public abstract class CumulativeImportance {
    protected int[][] directions;
    protected int height;
    protected int[][] importanceGrid;
    protected int width;
    protected List<SortableKeyValuePair> importances;
    
    public void applyTo(final int[][] pixels) {
        this.width = pixels.length;
        this.height = pixels[0].length;
        this.importances = new LinkedList<SortableKeyValuePair>();
        
        importanceGrid = new int[this.height][this.width];
        directions = new int[this.width][this.height];
        populateDirections(pixels);
    }

    protected void findImportanceAndDirection(final int[][] pixels, int x, int y) {
        int importance = pixels[x][y];
        int direction = getDirection(x, y);
        int minimalNeighbor = findMinimalNeighbor(x, y, direction);
        directions[x][y] = direction;
        importanceGrid[y][x] = importance + minimalNeighbor;
    }

    /** 
     * Finds the value of the minimal neighbor of the pixel at given coordinates
     * in the given direction.
     */
    protected abstract int findMinimalNeighbor(int x, int y, int direction);

    /**
     * Returns an array containing the cumulative importance of every line
     * starting on that row/column. 
     */
    protected abstract int[] getCumulativeImportance();

    /**
     * Figures out what direction the minimal neighbor can be found at.
     * @return -1, 0 or 1, meaning left, up, right or down, left, up respectively
     */
    protected abstract int getDirection(int x, int y);

    /**
     * Finds the index of the smallest value in the array returned by 
     * getCumulativeImportance().
     */
    public int getLeastImportantLine() {
        int value = Integer.MAX_VALUE;
        int key = 0;
        
        final int[] last = getCumulativeImportance();
        for (int i = 0; i < last.length; i++) {
            if (last[i] < value) {
                value = last[i];
                key = i;
            }
        }
        
        return key;
    }
    
    /**
     * Tries to find count non-overlapping lines of minimal importance
     * @param count
     * @return 
     */
    public abstract int[][] getLeastImportantLines(int count);

    /**
     * Calculates and returns the path that results in the cumulative importance
     * found at index idx.
     */
    public abstract int[] getLine(int idx);

    /**
     * Populates the directions-matrix.
     * @param pixels 
     */
    protected abstract void populateDirections(final int[][] pixels);

    /**
     * Populates the first row/column of the directions-matrix
     * @param pixels 
     */
    protected abstract void populateInitialImportance(final int[][] pixels);

    protected class SortableKeyValuePair implements Comparable<SortableKeyValuePair> {
        private final int key;
        private final int value;
        
        public SortableKeyValuePair(int key, int value) {
            this.key = key;
            this.value = value;
        }
        
        public int getKey() {
            return this.key;
        }
        
        public int getValue() {
            return this.value;
        }

        @Override
        public int compareTo(SortableKeyValuePair t) {
            return this.value - t.value;
        }
        
    }
}
