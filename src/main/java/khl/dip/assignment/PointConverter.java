
package khl.dip.assignment;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class PointConverter implements IStringConverter<Point> {

    @Override
    public Point convert(String param) {
        String[] coordinates = param.split("x");
            if (coordinates.length != 2) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }

            try {
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);

                return new Point(x, y);
            } catch (NumberFormatException e) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }
    }

}
