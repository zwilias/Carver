
package khl.dip.assignment;

import ij.process.ByteProcessor;
import java.util.Collections;
import java.util.LinkedList;

public class CumulativeImportance {
    private final int[][] ci;
    private final int[][] directions;
    private final ByteProcessor bp;
    
    public CumulativeImportance(final ByteProcessor bp) {
        this.bp = bp;
        
        // By saving these with the y as the first index, we can extract an entire
        // line later on - easier that way.
        ci = new int[bp.getHeight()][bp.getWidth()];
        directions = new int[bp.getWidth()][bp.getHeight()];
        
        // Copy the first line into the cumulativeImportance matrix
        for (int x = 0; x < bp.getWidth(); x++) {
            ci[0][x] = bp.getPixel(x, 0);
        }
        
        // Run through the rest of the lines, figuring out their minimal
        // cumulative importance along the way.
        for (int y = 1; y < bp.getHeight(); y++) {
            for (int x = 0; x < bp.getWidth(); x++) {
                int imp = bp.getPixel(x, y);
                int minUp;
                
                if (x == 0) {
                    if (ci[y-1][x] > ci[y-1][x+1]) {
                        directions[x][y] = 1;
                    }
                } else if (x == bp.getWidth()-1) {
                    if (ci[y-1][x-1] < ci[y-1][x]) {
                        directions[x][y] = -1;
                    }
                } else {
                    if (ci[y-1][x-1] < ci[y-1][x] && ci[y-1][x] <= ci[y-1][x+1]) {
                        // left smallest
                        directions[x][y] = -1;
                    } else if (ci[y-1][x-1] >= ci[y-1][x] && ci[y-1][x] > ci[y-1][x+1]) {
                        // right smallest
                        directions[x][y] = 1;
                    }
                }
                
                minUp = ci[y-1][x+directions[x][y]];
                ci[y][x] = imp+minUp;
            }
        }
    }
    
    public int[] getCumulativeImportance() {
        return ci[bp.getHeight()-1];
    }
    
    public int[] getLine(int x) {
        final int[] result = new int[bp.getHeight()];
        
        result[result.length-1] = x;
        
        for (int y = result.length-1; y > 0; y--) {
            x += directions[x][y];
            result[y-1] = x;
        }
        
        return result;
    }
    
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
    
    public int[] getLeastImportantLines(final int count) {
        final int[] result = new int[count];
        
        // First, make a List of SortableKeyValuePair's containing the key and values
        // of the CumulativeImportance result
        LinkedList<SortableKeyValuePair> list = new LinkedList<SortableKeyValuePair>();
        final int[] lastRow = getCumulativeImportance();
        for (int i = 0; i < lastRow.length; i++) {
            list.add(new SortableKeyValuePair(i, lastRow[i]));
        }
        
        // Sort that list
        Collections.sort(list);
        
        // Now get the first count elements
        for (int i = 0; i < count; i++) {
            result[i] = list.poll().getKey();
        }
        
        return result;
    }
    
    /**
     * Creates a matrix with a 0 for each pixel that should not be removed and a 1
     * for each pixel that should be removed. However, keep in mind that this whole
     * algorithm forgets one key aspect: if parts of the same line are used in multiple
     * least-important lines, they will only be removed once, creating images with
     * unequally sized line-lengths.
     * TODO: fix.
     * @param count the number of lines for which the least important line should be found
     * @return a matrix with a 0 for each pixel that should stay untouched and a 1 for each
     *  that should be removed.
     */
    public int[][] getLeastImportantAsMatrix(final int count) {
        final int[][] result = new int[bp.getWidth()][bp.getHeight()];
        
        final int height = bp.getHeight();
        
        final int[] lines = getLeastImportantLines(count);
        for (int line : lines) {
            int x = line;
            result[x][height-1] = 1;
            for (int y = height-1; y >= 0; y--) {
                x += ci[y-1][x];
                result[x][y-1] = 1;
            }
        }
        
        return result;
    }
    
    private class SortableKeyValuePair implements Comparable<SortableKeyValuePair> {
        private final int key;
        private final int value;
        
        public SortableKeyValuePair(final int key, final int value) {
            this.key = key;
            this.value = value;
        }
        
        public int getKey() {
            return this.key;
        }

        @Override
        public int compareTo(SortableKeyValuePair t) {
            return this.value - t.value;
        }
    }
}
