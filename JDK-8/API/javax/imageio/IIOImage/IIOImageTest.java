/*
 * @test
 * @summary javax.imageio.IIOImage conformance: behavioral.
 * @run main IIOImageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.util.List<java.awt.image.BufferedImage> thumbs = new java.util.ArrayList<>();
        javax.imageio.IIOImage iio = new javax.imageio.IIOImage(img, thumbs, null);
        check(iio.getRenderedImage() == img, "getRenderedImage");
        check(!iio.hasRaster(), "hasRaster false");
        check(iio.getNumThumbnails() == 0, "numThumbnails 0");
        check(iio.getThumbnails() != null, "getThumbnails non-null");
        check(iio.getMetadata() == null, "getMetadata null");
        iio.setThumbnails(thumbs);
        check(iio.getThumbnails() == thumbs, "setThumbnails");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
