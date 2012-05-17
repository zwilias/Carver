package khl.dip.assignment;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Hello world!
 *
 */
public class Carver 
{
    public static void main(String[] args) { 
        CarveParams carveParams = new CarveParams();
        JCommander jc = new JCommander();
        jc.addObject(carveParams);
        StringBuilder usage = new StringBuilder();
        jc.usage(usage);
        
        try {
            jc.parse(args);
            carveParams.checkParams();
            if (carveParams.showUsage) {
                System.out.println(usage);
            } else {
                new Carve(carveParams).run();
            }
        } catch (ParameterException ex) {
            usage.insert(0, ex.getMessage() + "\n\n");
            System.out.println(usage);
        }
    }
}
