package khl.dip.assignment;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) { 
        Carve carve = new Carve();
        JCommander jc = new JCommander();
        jc.addObject(carve);
        StringBuilder usage = new StringBuilder();
        jc.usage(usage);
        
        try {
            jc.parse(args);
            if (carve.isShowUsage()) {
                System.out.println(usage);
            } else {
                carve.run();
            }
            //Carve carve = new Carve(img);
            //carve.benchmark(5000);
            //carve.benchmarkImportance(500);
        } catch (ParameterException ex) {
            usage.insert(0, ex.getMessage() + "\n\n");
            System.out.println(usage);
        }
    }
}
