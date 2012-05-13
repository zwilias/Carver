
package khl.dip.assignment;

public class Sobel extends Gray8Convolution {
    public Sobel() {
        super(getKernel());
    }
    
    private static double[][] getKernel() {
        return new double[][]{
            {0, 1, 0}, 
            {1, -4, 1}, 
            {0, 1, 0}};
    }
}
