package khl.dip.assignment;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public final class Carver {
    private Carver() {}

    public static void main(final String[] args) {
        disableInfoLogging();
        
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
    
    protected static void disableInfoLogging() {
        //get the top Logger:
        final Logger topLogger = java.util.logging.Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }


        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        //set the console handler to fine:
        consoleHandler.setLevel(java.util.logging.Level.WARNING);
    }
}
