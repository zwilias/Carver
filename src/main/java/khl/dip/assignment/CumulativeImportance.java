package khl.dip.assignment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CumulativeImportance {
    public static final int PRIORPIXEL = 0;
    public static final int PROTPIXEL = 99999;
    private static final Logger LOGGER = Logger.getLogger(CumulativeImportance.class.toString());

    protected int[][] directions;
    protected int height;
    protected int[][] importanceGrid;
    protected int width;
    protected List<SortableKeyValuePair> importances;
    protected boolean[][] prioritized;
    protected boolean[][] protectedPixels;

    public void applyTo(final int[][] pixels, final boolean[][] prioritized, final boolean[][] protectedPixels) {
        this.width = pixels.length;
        this.height = pixels[0].length;
        this.importances = new LinkedList<SortableKeyValuePair>();
        this.prioritized = prioritized;
        this.protectedPixels = protectedPixels;

        importanceGrid = new int[this.height][this.width];
        directions = new int[this.width][this.height];
        populateDirections(pixels);
    }

    protected void findImportanceAndDirection(final int[][] pixels, final int x, final int y) {
        final int direction = getDirection(x, y);
        directions[x][y] = direction;

        if (prioritized[x][y]) {
            importanceGrid[y][x] = PRIORPIXEL;
        } else if (protectedPixels[x][y]) {
            importanceGrid[y][x] = PROTPIXEL;
        }
        
        else {
            final int importance = pixels[x][y];
            final int minimalNeighbor = findMinimalNeighbor(x, y, direction);
            importanceGrid[y][x] = importance + minimalNeighbor;
        }
    }

    

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

    protected int findMinimalNeighbor(final int x, final int y, final int direction) {
        return importanceGrid[y - 1][x + direction];
    }

    public int[] getCumulativeImportance() {
        return importanceGrid[this.height - 1];
    }

    protected int getDirection(final int x, final int y) {
        int direction = 0;
        if (x == 0) {
            if (importanceGrid[y - 1][x] > importanceGrid[y - 1][x + 1]) {
                direction = 1;
            }
        } else if (x == this.width - 1) {
            if (importanceGrid[y - 1][x - 1] < importanceGrid[y - 1][x]) {
                direction = -1;
            }
        } else {
            if (importanceGrid[y - 1][x - 1] < importanceGrid[y - 1][x] && importanceGrid[y - 1][x - 1] <= importanceGrid[y - 1][x + 1]) {
                // left smallest
                direction = -1;
            } else if (importanceGrid[y - 1][x + 1] < importanceGrid[y - 1][x] && importanceGrid[y - 1][x + 1] <= importanceGrid[y - 1][x - 1]) {
                // right smallest
                direction = 1;
            }
        }
        return direction;
    }

    public int[][] getLeastImportantLines(final int count) {
        int[][] usedMatrix = new int[this.width][this.height];
        int[] tmp;
        final LinkedList<SortableKeyValuePair> cumuls = new LinkedList<SortableKeyValuePair>();
        // First get all the cumulative importance things
        for (int i = 0; i < this.width; i++) {
            cumuls.add(new SortableKeyValuePair(i, importanceGrid[this.height - 1][i]));
        }
        Collections.sort(cumuls);
        int i = 0;
        while (i < count && !cumuls.isEmpty()) {
            tmp = getLine(cumuls.poll().getKey());
            // Find out if this line crosses any other line already in the array
            boolean conflict = false;
            for (int y = this.height - 1; !conflict && y >= 0; y--) {
                conflict = usedMatrix[tmp[y]][y] == 1;
            }
            // If it does, head on to the next line
            if (conflict) {
                continue;
            }
            // otherwise, mark every cell this one uses as used
            for (int y = this.height - 1; y >= 0; y--) {
                usedMatrix[tmp[y]][y] = 1;
            }
            i += 1;
        }
        int[][] sorted = new int[i][this.height];
        // Almost there..
        for (int y = 0; y < height; y++) {
            int cnt = 0;
            for (int x = 0; x < width; x++) {
                if (usedMatrix[x][y] == 1) {
                    sorted[cnt++][y] = x;
                }
            }
        }
        LOGGER.log(Level.INFO, "Found {0} non-colliding lines.", i);
        return sorted;
    }

    public int[] getLine(final int index) {
        final int[] result = new int[this.height];
        int localIdx = index;
        result[result.length - 1] = localIdx;
        for (int y = result.length - 1; y > 0; y--) {
            localIdx += directions[localIdx][y];
            result[y - 1] = localIdx;
        }
        return result;
    }

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

    protected void populateInitialImportance(final int[][] pixels) {
        // Copy the first line into the cumulativeImportance matrix
        for (int x = 0; x < this.width; x++) {
            importanceGrid[0][x] = pixels[x][0];
        }
    }

    

    protected class SortableKeyValuePair implements Comparable<SortableKeyValuePair> {

        private final int key;
        private final int value;

        public SortableKeyValuePair(final int key, final int value) {
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
        public int compareTo(final SortableKeyValuePair otherPair) {
            return this.value - otherPair.value;
        }
    }
}
