package khl.dip.assignment;

public class Gray8Max extends AbstractGray8NeighborhoodOperation {

    @Override
    protected int operation(final int[][] pixels, final int x, final int y) {
        final int width = pixels.length;
        final int height = pixels[0].length;
        int max = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                final int xi = x + i;
                final int yj = y + j;

                if (xi < 0 || yj < 0 || xi >= width || yj >= height) {
                    continue;
                }

                if (pixels[xi][yj] > max) {
                    max = pixels[xi][yj];
                }
            }
        }
        return max;
    }
}
