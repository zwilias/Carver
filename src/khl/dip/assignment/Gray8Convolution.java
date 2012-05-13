package khl.dip.assignment;

import ij.process.ByteProcessor;

public class Gray8Convolution extends Gray8NeighborhoodOperation {
    private double[][] kernel;
    
    public Gray8Convolution(double[][] kernel) {
        this.kernel = kernel;
    }

    protected int f(ByteProcessor ip, int x, int y) {
        double total = 0.0;

        for (int i = -(kernel.length - 1) / 2; i < (kernel.length - 1) / 2; i++) {
            for (int j = -(kernel[0].length - 1) / 2; j <= (kernel[0].length - 1) / 2; j++) {
                int ipX = x + i;
                int ipY = y + j;
                int kX = i + (kernel.length / 2);
                int kY = j + (kernel[0].length / 2);
                total += ip.getPixel(ipX, ipY) * kernel[kX][kY];
            }
        }

        return (int) total;
    }
}
