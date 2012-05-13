package khl.dip.assignment;

import ij.process.ByteProcessor;

public class Gray8Max extends Gray8NeighborhoodOperation {

    @Override
    protected int f(ByteProcessor ip, int x, int y) {
        int max = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (ip.getPixel(x + i, y + j) > max) {
                    max = ip.getPixel(x + i, y + j);
                }
            }
        }
        return max;
    }
}
