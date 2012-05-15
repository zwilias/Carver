package khl.dip.assignment;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import ij.ImagePlus;
import ij.gui.ImageWindow;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) { 
        try {
            Carve carve = new Carve();
            JCommander jc = new JCommander(carve, args);
            carve.run();
            //Carve carve = new Carve(img);
            //carve.benchmark(5000);
            //carve.benchmarkImportance(500);
            ImagePlus img = carve.getImage();

            ImageWindow window = new ImageWindow(img);
            window.setVisible(true);
        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
