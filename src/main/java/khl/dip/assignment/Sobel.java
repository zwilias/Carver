package khl.dip.assignment;

public class Sobel extends Gray8Convolution {

    private static final double[][] KERNEL = new double[][]{
        {0, 1, 0},
        {1, -4, 1},
        {0, 1, 0}};

    public Sobel() {
        super(KERNEL);
    }
}
