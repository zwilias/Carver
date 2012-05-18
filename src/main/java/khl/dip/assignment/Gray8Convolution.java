package khl.dip.assignment;

import ij.process.ByteProcessor;

public class Gray8Convolution extends Gray8NeighborhoodOperation {

    private final double[][] kernel;

    public Gray8Convolution(final double[][] kernel) {
        this.kernel = kernel;
    }

    @Override
    protected int f(int[][] pixels, int x, int y) {
        double total = 0.0;
        final int kWidth = kernel.length;
        final int kHeight = kernel[0].length;
        final int iWidth = pixels.length;
        final int iHeight = pixels[0].length;

        for (int i = -(kWidth - 1) / 2; i < (kWidth - 1) / 2; i++) {
            for (int j = -(kHeight - 1) / 2; j <= (kHeight - 1) / 2; j++) {
                int ipX = x + i;
                int ipY = y + j;
                if (ipX < 0 || ipY < 0 || ipX >= iWidth || ipY >= iHeight) {
                    continue;
                }
                int kX = i + (kWidth / 2);
                int kY = j + (kHeight / 2);
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
