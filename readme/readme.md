% DIP Assignment - Content Aware Scaling
% Ilias Van Peer, 2TX3/4
% May 20, 2012

# Carver #
## Installation ##

**Carver** is a [maven project](http://maven.apache.org/). This is mostly for automatic dependency resolution and as a way of experimenting with maven. However, ImageJ is not part of the default maven repository, so after installing maven, ImageJ can be added to your local repository like this:

```
mvn install:install-file -DgroupId=gov.nih -DartifactId=ij -Dversion=1.0 -Dpackaging=jar -Dfile=/path/to/ij.jar
```

After that, executing `mvn install` in the directory where `pom.xml` is located will create a directory named `target` containing a number of files, including `carver.jar` which is the final, executable jar-file including all dependencies.

It can then be ran with `java -jar target/carver.jar`.

## Features ##

Let's have a look at the output of `java -jar target/carver.jar`:

```
carver/ › java -jar target/carver.jar 
The following options are required: -i, --input 

Usage: java -jar carver.jar [options]
  Options:
    -a, --add-lines              When set, the image will be enlarged, not
                                 shrinked.
                                 Default: false
    -fb, --facebookAccessToken   A facebook OAuth access token. Check the readme
                                 for details on how to obtain and use this. When
                                 provided, the picture will also be uploaded to facebook.
        --help                   Show usage.
                                 Default: false
    -h, --horizontal             Number of horizontal lines to be removed or
                                 added.
                                 Default: 0
  * -i, --input                  Input image
    -c, --lines-per-batch        How many lines will be removed in each batched
                                 action. Setting this to 1 will bypass batching.
                                 Default: 30
    -o, --output                 File to write the carved image to. (If not
                                 provided, image is displayed.
    -p, --prioritize             Comma-separated list of pixels to prioritize.
                                 Each pixel is in the format of XxY, with X its
                                 x-coordinate, 0-indexed on the left and Y its y-coordinate,
                                 0-indexed on the top. These will be processed in the
                                 order in which they're supplied to create a shape
                                 with the supplied pixels functioning as
                                 corner-points.
                                 Default: []
    -pi, --prioritizedPoint      A point that lies inside the shape described by
                                 the parameters to -p/--prioritize. Used to fill the
                                 shape. Mandatory when using -p/--priorize.
    -s, --protect                Comma-separated list of pixels to protect. Each
                                 pixel is in the format of XxY, with X its
                                 x-coordinate, 0-indexed on the left and Y its y-coordinate,
                                 0-indexed on the top. These will be processed in the
                                 order in which they're supplied to create a shape
                                 with the supplied pixels functioning as
                                 corner-points.
                                 Default: []
    -si, --protectedPoint        A point that lies inside the shape described by
                                 the parameters to -s/--protect. Used to fill the
                                 shape. Mandatory when using -s/--protect.
    -v, --vertical               Number of vertical lines to be removed or
                                 added.
                                 Default: 0
```

One thing that isn't mentioned in the usage output is that, as a side effect of using [JCommander](http://jcommander.org/) for parameter parsing, carver supports using the `@` syntax as well. This means that all the options can be put into a file, and this file can be passed with the `@` option. All the examples shown in this readme can be found in the `examples/` directory and can be executed using the `@` syntax.

### Basic usage ###

The implementation of the approach described in the assignment can be used as follows:

```
java -jar target/carver.jar -c 1 -v 200 -i images/tower.png
```

This will take *Figure 1*, remove 200 vertical lines, line by line, and will display *Figure 2* on your screen.[^example1] The `-v` and `-i` parameters are self-explanatory. The `-c 1` parameter merely means we want to bypass the [batch-mode optimization](#optimization) and handle this line by line.

[^example1]: This command can also be executed as `java -jar target/carver.jar @examples/example1`

![Figure 1. images/tower.png \label{fig1}](images/tower.png)

![Figure 2. After carving](images/tower-example1.png)

### Changing the height ###

As well as horizontal resizing, we can change the height by supplying the number of horizontal lines that must be removed using `-h` or `--horizontal`. For example, we can remove 200 vertical lines as well as 100 horizontal lines from *Figure 1* using the following command, yielding us *Figure 3*.[^example2]

```
java -jar target/carver.jar -c 1 -v 200 -h 100 -i images/tower.png
```

![Figure 3. Changing the height](images/tower-example2.png)

[^example2]: Again, this command can also be executed by running `java -jar target/carver.jar @examples/example2`

### Optimization ###

Batch-mode (i.e. selecting multiple non-overlapping low-importance rows) removal proved to be a huge performance booster. Let's compare some metrics on batch-processing vs line-by-line processing:

```
carver/ › time java -jar target/carver.jar -v 200 -c 1 -i images/tower.png -o /tmp/o.png
java -jar target/carver.jar -v 200 -c 1 -i images/tower.png -o /tmp/o.png  17.42s user 0.50s system 64% cpu 27.716 total

carver/ › time java -jar target/carver.jar -v 200 -c 30 -i images/tower.png -o /tmp/o.png 
java -jar target/carver.jar -v 200 -c 30 -i images/tower.png -o /tmp/o.png  2.61s user 0.25s system 73% cpu 3.895 total

carver/ › time java -jar target/carver.jar -v 200 -c 200 -i images/tower.png -o /tmp/o.png
java -jar target/carver.jar -v 200 -c 200 -i images/tower.png -o /tmp/o.png  1.67s user 0.16s system 63% cpu 2.858 total
```

Clearly, this is a huge win. The default number of lines processed in each batch is 30. This seems to give a good balance between speed and actually selecting nonimportant lines on regular images. Setting the number too high (for example 200) will result in an image that looks like it has been resized with a regular algorithm, as it will have to select more and more important lines due to collisions in the less important lines, which are resolved after actually removing the line and re-running the Sobel and Gray8Max filters.

Other than batch-mode processing, I tried to use the `final` modifier wherever possible and used an `int[][]` instead of a `ByteProcessor` in the intermediary steps. Besides being slightly faster, this also allowed stricter bounds checking due to `ArrayIndexOutOfBounds` errors which `ByteProcessor` doesn't throw.

However, there are still many things that could be severely optimized left.

  - Not desaturating after every batch

    It would be trivial to pass the `int[][]` generated by the desaturation algorithm to the `LineChanger`, so it can remove/add rows as applicable, thus preventing the need to desaturate the intermediary image all the time.

  - Only Applying the Gray8Max and Sobel filters where necessary.

    Currently, the Sobel and Gray8Max filters are applied to the whole image after every batch. This could be changed to only process the areas around the removed line(s), as they're both just 3x3 filters.

  - ...

Finally, images look a little more natural when they were scaled using batch-processing. However, as mentioned before, the number of lines shouldn't be set too high. As a matter of reference, the tower image after removing 200 vertical lines, removing 30 lines and 200 lines at once respectively.

![With -c 30](images/tower-30.png)

![With -c 200](images/tower-200.png)

### Protecting and prioritizing pixels ###

Using the `-p/--prioritize` option with the `-pi/--prioritizedPoint` option or the `-s/--protect` and the `-si/--protectedPoint` options allows prioritizing and protected regions. Both option-pairs work similarly. Using `-p` (or `-s`), any number of pixels is supplied, which will be used to generate a shape by calculating the lines connecting each of the pixels as well as the line connecting the last to the first of those pixels. Then the pixel provided by the `-pi` option (or the `-si` option) is used as a seed for a simple flood filling algorithm, which steps through all the pixels around the selected pixel, marking them as filled. This raster of pixels is then used while calculating the cumulative importance, overwriting the calculated values with either -99999 when prioritizing or +99999 when protecting pixels. During the removal/addition of lines, these rasters are updated accordingly.

Sadly, however, priorization doesn't work well in combination with batching - too many collisions will occur in the area under the prioritized area. As such batching must be disabled. But that's not all; our algorithms are limited to their very direct neighbours, which makes them fast but also limits the "attraction" that a prioritized area would otherwise have, thus limiting the lines affected by it to the lines that would already be passing through the area. As such, the results look... Wonky. As an example; consider *Figure 4*[^example3], which is our good old `tower.png`, minus the lady.

```
java -jar target/carver.jar -v 70 -c 1 -p 58x403,80x403,83x441,70x541,58x441 -pi 70x428 -i images/tower.png
```

[^example3]: And once more: `java -jar target/carver.java @examples/example3`

![Figure 4. Prioritizing an area](images/tower-example3.png)

### Enlarging Images ###

This was a fairly simple feature to implement, though it does "require" batchmode in order to make the result look decent - the added rows will automatically seem less important due to them being the average of their neighbors. For example:[^example4]

```
java -jar target/carver.jar -v 100 -h 50 -a -i images/tower.png
```

[^example4]: And finally: `java -jar target/carver.java @examples/example4`

![Figure 5. Enlarged image](images/tower-example4.png)

### Upload to Facebook ###

Mostly out of curiosity, I also implemented uploading to facebook using the simple and flexible [RestFB Facebook Graph API Client Library](http://restfb.com/). Uploading to facebook, however, requires the use of an OAuth Access Token. This is bound to both the application and the user, so for now, we just ask for the Access Token to be passed in as a parameter with `-fb`.

Acquiring an access token is quite simple. Go to the [Graph API Explorer](https://developers.facebook.com/tools/explorer) tool, hit "Get Access Token", make sure `publish_stream` is checked under "Extended Permissions", hit "Get Access Token", and copy-paste the token facebook generated. When supplied through the `-fb` parameter, carver will upload the picture to facebook as well as execute the default action (i.e. displaying it or writing it to a file when `-o` is supplied.)

It will spit out a link to the image, and that's all there is to it.

## References and links ##

Carver is built on a number of libraries:

- Good old [ImageJ](http://rsbweb.nih.gov/ij/) for manipulating images.
- The pretty awesome, annotation based command-line argument parser [JCommander](http://jcommander.org/).
- The amazingly simple [RestFB](http://restfb.com/) [Facebook Graph API](https://developers.facebook.com/docs/reference/api/) Client library.
- The fairly straightforward [Maven](http://maven.apache.org/) "software project management and comprehension tool".

Another tool used during the development of carver is the "don't feel too proud of this code, son" tool [PMD](http://pmd.sourceforge.net/pmd-5.0.0/). It has proven to be quite useful in spotting all kinds of problems early on.

This readme file was created using [pandoc](http://johnmacfarlane.net/pandoc/), the universal document converter.

The "rendering a line on a raster based on 2 endpoints" algorithm used in carver is an implementation of the pseudo-code version of [Bresenham's line algorithm](http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#Simplification) as found on wikipedia.

Finally, [github](http://github.com) is pretty awesome.
