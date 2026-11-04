/*
 * @test
 * @summary javax.imageio.ImageTypeSpecifier conformance: behavioral.
 * @run main ImageTypeSpecifierTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageTypeSpecifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.ImageTypeSpecifier its = javax.imageio.ImageTypeSpecifier.createFromBufferedImageType(java.awt.image.BufferedImage.TYPE_INT_RGB);
        check(its != null, "non-null");
        check(its.getBufferedImageType() == java.awt.image.BufferedImage.TYPE_INT_RGB, "type");
        check(its.getNumComponents() == 3, "numComponents 3");
        check(its.getNumBands() == 3, "numBands 3");
        check(its.getBitsPerBand(0) == 8, "bitsPerBand 8");
        check(its.getSampleModel() != null, "sampleModel");
        check(its.getColorModel() != null, "colorModel");
        java.awt.image.BufferedImage bi = its.createBufferedImage(4, 4);
        check(bi.getWidth() == 4 && bi.getHeight() == 4, "createBufferedImage");
        javax.imageio.ImageTypeSpecifier g = javax.imageio.ImageTypeSpecifier.createGrayscale(8, java.awt.image.DataBuffer.TYPE_BYTE, false);
        check(g.getNumComponents() == 1, "grayscale 1 component");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
