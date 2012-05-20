% DIP Assignment - Content Aware Scaling
% Ilias Van Peer, 2TX3/4
% May 20, 2012

# Carver #
## Installation ##

`carver` is a [maven project](http://maven.apache.org/). This is mostly for automatic dependency resolution and as a way of experimenting with maven. However, ImageJ is not part of the default maven repository, so after installing maven, ImageJ can be added to your local repository like this:

```
mvn install:install-file -DgroupId=gov.nih -DartifactId=ij -Dversion=1.0 -Dpackaging=jar -Dfile=/path/to/ij.jar
```

After that, executing `mvn install` in the directory where `pom.xml` is located will create a directory named `target` containing a number of files, including `carver.jar` which is the final, executable jar-file including all dependencies.

It can then be ran with `java -jar target/carver.jar`.

## Features ##

Let's have a look at the output of `java -jar target/carver.jar`:

```shell
carver/ › java -jar target/carver.jar 
The following options are required: -i, --input 

Usage: java -jar carver.jar [options]
  Options:
    -a, --add-lines           When set, the image will be enlarged, not
                              shrinked.
                              Default: false
        --help                Show usage.
                              Default: false
    -h, --horizontal          Number of horizontal lines to be removed or added.
                              Default: 0
  * -i, --input               Input image
    -c, --lines-per-batch     How many lines will be removed in each batched
                              action. Setting this to 1 will bypass batching.
                              Default: 30
    -o, --output              File to write the carved image to. (If not
                              provided, image is displayed.
    -p, --prioritize          Comma-separated list of pixels to prioritize. Each
                              pixel is in the format of XxY, with X its x-coordinate,
                              0-indexed on the left and Y its y-coordinate, 0-indexed on
                              the top. These will be processed in the order in which
                              they're supplied to create a shape with the supplied
                              pixels functioning as corner-points.
                              Default: []
    -pi, --prioritizedPoint   A point that lies inside the shape described by
                              the parameters to -p/--prioritize. Used to fill the
                              shape. Mandatory when using -p/--priorize.
    -s, --protect             Comma-separated list of pixels to protect. Each
                              pixel is in the format of XxY, with X its x-coordinate,
                              0-indexed on the left and Y its y-coordinate, 0-indexed on
                              the top. These will be processed in the order in which
                              they're supplied to create a shape with the supplied
                              pixels functioning as corner-points.
                              Default: []
    -si, --protectedPoint     A point that lies inside the shape described by
                              the parameters to -s/--protect. Used to fill the
                              shape. Mandatory when using -s/--protect.
    -v, --vertical            Number of vertical lines to be removed or added.
                              Default: 0
```

## Final thoughts ##

