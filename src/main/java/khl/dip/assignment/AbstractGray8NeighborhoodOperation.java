package khl.dip.assignment;

public abstract class AbstractGray8NeighborhoodOperation {

    public int[][] applyTo(final int[][] pixels) {
        final int[][] result = new int[pixels.length][pixels[0].length];

        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[0].length; y++) {
                result[x][y] = operation(pixels, x, y);
            }
        }

        return result;
    }

    protected abstract int operation(final int[][] pixels, final int x, final int y);
}
