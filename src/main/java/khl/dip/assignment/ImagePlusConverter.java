
package khl.dip.assignment;

import com.beust.jcommander.IStringConverter;
import ij.ImagePlus;

public class ImagePlusConverter implements IStringConverter<ImagePlus> {

    @Override
    public ImagePlus convert(String string) {
        return new ImagePlus(string);
    }

}
