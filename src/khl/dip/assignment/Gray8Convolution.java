package khl.dip.assignment;

import ij.process.ByteProcessor;

public class Gray8Convolution extends Gray8NeighborhoodOperation {
    private final double[][] kernel;
    
    public Gray8Convolution(final double[][] kernel) {
        this.kernel = kernel;
    }

    @Override
    protected int f(final ByteProcessor ip, final int x, final int y) {
        double total = 0.0;
        final int kWidth = kernel.length;
        final int kHeight = kernel[0].length;

        for (int i = -(kWidth - 1) / 2; i < (kWidth - 1) / 2; i++) {
            for (int j = -(kHeight - 1) / 2; j <= (kHeight - 1) / 2; j++) {
                int ipX = x + i;
                int ipY = y + j;
                int kX = i + (kWidth / 2);
                int kY = j + (kHeight / 2);
                total += ip.getPixel(ipX, ipY) * kernel[kX][kY];
            }
        }

        return (int) total;
    }
}
