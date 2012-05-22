package khl.dip.assignment;

public class Sobel extends Gray8Convolution {

    private static final double[][] KERNEL_X = new double[][]{
        {-1, 0, 1},
        {-2, 0, 2},
        {-1, 1, 1}};
    
    private static final double[][] KERNEL_Y = new double[][]{
        { 1,  2,  1},
        { 0,  0,  0},
        {-1, -2, -1}};

    public Sobel() {
        super(KERNEL_X, KERNEL_Y);
    }
}
