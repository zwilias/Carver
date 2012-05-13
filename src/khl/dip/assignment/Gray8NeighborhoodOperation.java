package khl.dip.assignment;

import ij.process.ByteProcessor;

public abstract class Gray8NeighborhoodOperation {

    public Gray8NeighborhoodOperation() {
    }

    public void applyTo(ByteProcessor ip) {
        ByteProcessor copy = (ByteProcessor) ip.duplicate();
        for (int x = 0; x < ip.getWidth(); x++) {
            for (int y = 0; y < ip.getHeight(); y++) {
                int newColor = f(copy, x, y);
                ip.putPixel(x, y, newColor);
            }
        }
    }

    protected abstract int f(ByteProcessor ip, int x, int y);
}
