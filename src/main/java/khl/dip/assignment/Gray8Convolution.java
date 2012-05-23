package khl.dip.assignment;

public class Gray8Convolution extends AbstractGray8NeighborhoodOperation {

    private final double[][] kernel;

    public Gray8Convolution(final double[][] kernel) {
        super();
        this.kernel = kernel;
    }

    @Override
    protected int operation(final int[][] pixels, final int x, final int y) {
        double total = 0.0;
        final int kWidth = kernel.length;
        final int kHeight = kernel[0].length;
        final int iWidth = pixels.length;
        final int iHeight = pixels[0].length;

        for (int i = -(kWidth - 1) / 2; i <= (kWidth - 1) / 2; i++) {
            for (int j = -(kHeight - 1) / 2; j <= (kHeight - 1) / 2; j++) {
                final int ipX = x + i;
                final int ipY = y + j;
                if (ipX < 0 || ipY < 0 || ipX >= iWidth || ipY >= iHeight) {
                    continue;
                }
                final int kX = i + (kWidth / 2);
                final int kY = j + (kHeight / 2);
                total += pixels[ipX][ipY] * kernel[kX][kY];
            }
        }

        return clamp((int) total);
    }

    public int clamp(final int val) {
        return val < 0
                ? 0
                : val > 255
                ? 255
                : val;
    }
}
