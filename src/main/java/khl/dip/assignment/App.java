package khl.dip.assignment;

import ij.ImagePlus;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        ImagePlus img = new ImagePlus("tower.png");
        
        //Carve carve = new Carve(img, 200);
        Carve carve = new Carve(img);
        //carve.benchmark(5000);
        carve.benchmarkImportance(500);
        //img = carve.getImage();
        
        //ImageWindow window = new ImageWindow(img);
        //window.setVisible(true);
    }
}
