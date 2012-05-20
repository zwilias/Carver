package khl.dip.assignment;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Hello world!
 *
 */
public final class Carver {
    private Carver() {}

    public static void main(final String[] args) {
        final CarveParams carveParams = new CarveParams();
        final JCommander jCommander = new JCommander();
        jCommander.setProgramName("java -jar carver.jar");
        jCommander.addObject(carveParams);
        final StringBuilder usage = new StringBuilder();
        jCommander.usage(usage);

        try {
            jCommander.parse(args);
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
