package khl.dip.assignment;

import java.util.Collections;
import java.util.LinkedList;

public class CumulativeHorizontalImportance extends AbstractCumulativeImportance {

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
    protected int findMinimalNeighbor(final int x, final int y, final int direction) {
        return importanceGrid[y + direction][x - 1];
    }

    @Override
    protected int getDirection(final int x, final int y) {
        int direction = 0;
        if (y == 0) {
            if (importanceGrid[y][x - 1] >= importanceGrid[y + 1][x - 1]) {
                direction = 1;
            }
        } else if (y == this.height - 1) {
            if (importanceGrid[y - 1][x - 1] <= importanceGrid[y][x - 1]) {
                direction = -1;
            }
        } else {
            if (importanceGrid[y - 1][x - 1] <= importanceGrid[y][x - 1] && importanceGrid[y][x - 1] <= importanceGrid[y + 1][x - 1]) {
                // up smallest
                direction = -1;
            } else if (importanceGrid[y - 1][x - 1] >= importanceGrid[y][x - 1] && importanceGrid[y][x - 1] >= importanceGrid[y + 1][x - 1]) {
                // down smallest
                direction = 1;
            }
        }
        return direction;
    }

    @Override
    public int[] getCumulativeImportance() {
        int[] result = new int[this.height];
        for (int i = 0; i < this.height; i++) {
            result[i] = importanceGrid[i][this.width - 1];
        }
        return result;
    }

    @Override
    public int[] getLine(final int idx) {
        final int[] result = new int[this.width];

        int local = idx;
        result[result.length - 1] = idx;

        for (int x = result.length - 1; x > 0; x--) {
            local += directions[x][local];
            result[x - 1] = local;
        }

        return result;
    }

    @Override
    public int[][] getLeastImportantLines(final int count) {
        int[][] sorted = new int[count][this.width];
        int[][] usedMatrix = new int[this.width][this.height];
        int[] tmp;
        final LinkedList<SortableKeyValuePair> cumuls = new LinkedList<SortableKeyValuePair>();

        // First get all the cumulative importance things
        for (int i = 0; i < this.height; i++) {
            cumuls.add(new SortableKeyValuePair(i, importanceGrid[i][this.width - 1]));
        }

        Collections.sort(cumuls);

        int i = 0;

        while (i < count && !cumuls.isEmpty()) {
            tmp = getLine(cumuls.poll().getKey());

            // Find out if this line crosses any other line already in the array
            boolean conflict = false;
            for (int x = this.width - 1; !conflict && x >= 0; x--) {
                conflict = usedMatrix[x][tmp[x]] == 1;
            }

            // If it does, head on to the next line
            if (conflict) {
                continue;
            }

            // otherwise, mark every cell this one uses as used
            for (int x = this.width - 1; x >= 0; x--) {
                usedMatrix[x][tmp[x]] = 1;
            }

            i += 1;
        }

        // Let's finish up by creating our actual result
        for (int x = 0; x < width; x++) {
            int cnt = 0;
            for (int y = 0; y < height; y++) {
                if (usedMatrix[x][y] == 1) {
                    sorted[cnt++][x] = y;
                }
            }
        }

        return sorted;
    }
}
