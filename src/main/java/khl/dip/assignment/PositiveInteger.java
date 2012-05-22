package khl.dip.assignment;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PositiveInteger implements IParameterValidator {

    public void validate(String name, String value)
            throws ParameterException {
        try {
            int n = Integer.parseInt(value);
            if (n < 0) {
                throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
            }
        } catch (NumberFormatException e) {
            throw new ParameterException("Parameter " + name + " should be a number (found " + value + ")");
        }
    }
}
