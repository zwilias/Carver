
package khl.dip.assignment;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class PointConverter implements IStringConverter<Point> {

    @Override
    public Point convert(final String param) {
        final String[] coordinates = param.split("x");
            if (coordinates.length != 2) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }

            try {
                final int x = Integer.parseInt(coordinates[0]);
                final int y = Integer.parseInt(coordinates[1]);

                return new Point(x, y);
            } catch (NumberFormatException e) {
                throw new ParameterException("Coordinate '" + param + "' is not valid.");
            }
    }

}
