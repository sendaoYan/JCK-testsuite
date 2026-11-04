/*
 * @test
 * @summary javax.imageio.ImageReadParam conformance: behavioral.
 * @run main ImageReadParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

public class ImageReadParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected == t.getClass(),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    public static void main(String[] a) throws Exception {
        javax.imageio.ImageReadParam p = new javax.imageio.ImageReadParam();
        check(p.getSourceRegion() == null, "sourceRegion null");
        java.awt.Rectangle r = new java.awt.Rectangle(0, 0, 10, 10);
        p.setSourceRegion(r);
        check(p.getSourceRegion().equals(r), "setSourceRegion");
        p.setSourceSubsampling(2, 3, 1, 2);
        check(p.getSourceXSubsampling() == 2, "xSub");
        check(p.getSourceYSubsampling() == 3, "ySub");
        check(p.getSubsamplingXOffset() == 1, "xOff");
        check(p.getSubsamplingYOffset() == 2, "yOff");
        p.setSourceBands(new int[]{0});
        check(p.getSourceBands().length == 1, "sourceBands");
        check(p.getDestination() == null, "destination null");
        check(!p.canSetSourceRenderSize(), "canSetSourceRenderSize false");
        p.setSourceProgressivePasses(1, 5);
        check(p.getSourceMinProgressivePass() == 1, "minPass");
        check(p.getSourceNumProgressivePasses() == 5, "numPass");
        check(!p.hasController(), "hasController false");
        check(p.getDestinationBands() == null, "destinationBands null");
        check(p.getSourceRenderSize() == null, "sourceRenderSize null");
        check(p.getSourceMaxProgressivePass() == 5, "maxPass min+num-1");
        testStructure();
        testDestinationContract();
        testDestinationBands();
        testRenderSize();
        testProgressivePasses();
        testEndToEndWithPng();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageReadParam");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.IIOParam"), "extends IIOParam");
        Set<String> dm = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setDestination"), "method setDestination");
        check(dm.contains("getDestination"), "method getDestination");
        check(dm.contains("setDestinationBands"), "method setDestinationBands");
        check(dm.contains("getDestinationBands"), "method getDestinationBands");
        check(dm.contains("setDestinationType"), "method setDestinationType");
        check(dm.contains("canSetSourceRenderSize"), "method canSetSourceRenderSize");
        check(dm.contains("setSourceRenderSize"), "method setSourceRenderSize");
        check(dm.contains("getSourceRenderSize"), "method getSourceRenderSize");
        check(dm.contains("setSourceProgressivePasses"), "method setSourceProgressivePasses");
        check(dm.contains("getSourceMinProgressivePass"), "method getSourceMinProgressivePass");
        check(dm.contains("getSourceMaxProgressivePass"), "method getSourceMaxProgressivePass");
        check(dm.contains("getSourceNumProgressivePasses"), "method getSourceNumProgressivePasses");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
    }

    // destination stored by reference; setDestinationType clears destination
    static void testDestinationContract() {
        ImageReadParam p = new ImageReadParam();
        BufferedImage dest = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        p.setDestination(dest);
        check(p.getDestination() == dest, "destination stored by reference");
        p.setDestination(null);
        check(p.getDestination() == null, "setDestination null clears");
        p.setDestination(dest);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        p.setDestinationType(its);
        check(p.getDestination() == null, "setDestinationType clears destination");
        check(p.getDestinationType() == its, "setDestinationType sets type");
        p.setDestination(dest);
        p.setDestinationType(null);
        check(p.getDestinationType() == null, "setDestinationType null clears type");
        // actual behavior: the override clears the destination even for a null type
        check(p.getDestination() == null, "setDestinationType(null) also clears destination");
    }

    static void testDestinationBands() {
        ImageReadParam p = new ImageReadParam();
        int[] bands = new int[]{3, 2, 1, 0};
        p.setDestinationBands(bands);
        int[] got = p.getDestinationBands();
        check(got.length == 4 && got[0] == 3 && got[3] == 0, "destinationBands round-trip");
        check(got != bands, "getDestinationBands returns a copy");
        bands[0] = 9;
        check(p.getDestinationBands()[0] == 3, "setDestinationBands clones input");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationBands(new int[]{1, 1}), "duplicate band");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationBands(new int[]{-2}), "negative band");
        check(p.getDestinationBands().length == 4, "failed update leaves bands intact");
        p.setDestinationBands(null);
        check(p.getDestinationBands() == null, "setDestinationBands null clears");
        p.setDestinationBands(new int[0]);
        check(p.getDestinationBands().length == 0, "empty destination band array accepted");
    }

    // canSetSourceRenderSize is always false for the plain class (probed UOE)
    static void testRenderSize() {
        ImageReadParam p = new ImageReadParam();
        check(!p.canSetSourceRenderSize(), "canSetSourceRenderSize false");
        check(p.getSourceRenderSize() == null, "sourceRenderSize null");
        expectThrows(UnsupportedOperationException.class,
                     () -> p.setSourceRenderSize(new Dimension(8, 8)), "setSourceRenderSize unsupported");
        expectThrows(UnsupportedOperationException.class,
                     () -> p.setSourceRenderSize(null), "setSourceRenderSize null also unsupported");
    }

    static void testProgressivePasses() {
        ImageReadParam p = new ImageReadParam();
        check(p.getSourceMinProgressivePass() == 0, "default min pass 0");
        check(p.getSourceNumProgressivePasses() == Integer.MAX_VALUE, "default num passes MAX_VALUE");
        check(p.getSourceMaxProgressivePass() == Integer.MAX_VALUE, "default max pass MAX_VALUE");
        p.setSourceProgressivePasses(1, 5);
        check(p.getSourceMinProgressivePass() == 1, "min pass 1");
        check(p.getSourceNumProgressivePasses() == 5, "num passes 5");
        check(p.getSourceMaxProgressivePass() == 5, "max pass = min + num - 1");
        p.setSourceProgressivePasses(0, Integer.MAX_VALUE);
        check(p.getSourceMaxProgressivePass() == Integer.MAX_VALUE, "max pass MAX_VALUE without overflow");
        p.setSourceProgressivePasses(Integer.MAX_VALUE, 1);
        check(p.getSourceMinProgressivePass() == Integer.MAX_VALUE, "min pass MAX_VALUE");
        check(p.getSourceMaxProgressivePass() == Integer.MAX_VALUE, "max pass MAX_VALUE at boundary");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceProgressivePasses(-1, 1), "negative min pass");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceProgressivePasses(0, 0), "zero num passes");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceProgressivePasses(0, -1), "negative num passes");
        expectThrows(IllegalArgumentException.class,
                     () -> p.setSourceProgressivePasses(Integer.MAX_VALUE, 2), "min+num-1 overflow");
        check(p.getSourceMinProgressivePass() == Integer.MAX_VALUE, "failed pass update leaves state intact");
        p.setSourceProgressivePasses(0, Integer.MAX_VALUE);
        check(p.getSourceNumProgressivePasses() == Integer.MAX_VALUE, "reset to default passes");
    }

    // end-to-end: destination receives decoded pixels from a real PNG read
    static void testEndToEndWithPng() throws Exception {
        BufferedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xFF123456);
        src.setRGB(3, 3, 0xFF654321);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(src, "png", bos);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        ImageReadParam rp = reader.getDefaultReadParam();
        check(rp != null, "default read param non-null");
        BufferedImage target = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        rp.setDestination(target);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        reader.setInput(iis);
        BufferedImage result = reader.read(0, rp);
        check(result == target, "read returns the supplied destination");
        check(target.getRGB(0, 0) == 0xFF123456, "destination received pixel (0,0)");
        check(target.getRGB(3, 3) == 0xFF654321, "destination received pixel (3,3)");
        // source region + subsampling combination against the same PNG
        ImageReadParam rp2 = reader.getDefaultReadParam();
        rp2.setSourceRegion(new java.awt.Rectangle(0, 0, 2, 2));
        rp2.setSourceSubsampling(2, 2, 0, 0);
        BufferedImage sub = reader.read(0, rp2);
        check(sub.getWidth() == 1 && sub.getHeight() == 1, "region+subsampling yields 1x1");
        check(sub.getRGB(0, 0) == 0xFF123456, "subsampled pixel (0,0)");
        reader.dispose();
        iis.close();
    }
}
