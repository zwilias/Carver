package khl.dip.assignment;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import ij.ImagePlus;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImagePlusConverter implements IStringConverter<ImagePlus> {

    @Override
    public ImagePlus convert(final String string) {
        try {
            final File file = new File(string);
            final BufferedImage inMemory = ImageIO.read(file);
            return new ImagePlus(file.getName(), inMemory);
        } catch (IOException ex) {
            throw new ParameterException("Couldn't read image from '" + string + "'.");
        }
    }
}
