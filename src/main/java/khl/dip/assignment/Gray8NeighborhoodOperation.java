package khl.dip.assignment;

import ij.process.ByteProcessor;

public abstract class Gray8NeighborhoodOperation {

    public Gray8NeighborhoodOperation() {
    }
    
    public int[][] applyTo(final int[][] pixels) {
        final int[][] result = new int[pixels.length][pixels[0].length];
        
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[0].length; y++) {
                result[x][y] = f(pixels, x, y);
            }
        }
        
        return result;
    }

    protected abstract int f(final int[][] pixels, final int x, final int y);
}
