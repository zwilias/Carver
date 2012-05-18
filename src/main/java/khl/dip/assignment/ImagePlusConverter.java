package khl.dip.assignment;

import com.beust.jcommander.IStringConverter;
import ij.ImagePlus;

public class ImagePlusConverter implements IStringConverter<ImagePlus> {

    //TODO: use imageIO to open this, so we get an Exception at load time, not an NPE later on
    @Override
    public ImagePlus convert(String string) {
        return new ImagePlus(string);
    }
}
