/*
 * @test
 * @summary java.awt.image.PackedColorModel conformance: white-box structure plus black-box behavior.
 * @run main PackedColorModelTest
 */

import java.awt.color.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.WritableRaster;

public class PackedColorModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.PackedColorModel");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        check(ColorModel.class.isAssignableFrom(c), "extends ColorModel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PackedColorModel pcm = new PackedColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), 32,
            new int[]{0x00FF0000, 0x0000FF00, 0x000000FF}, 0xFF000000,
            false, ColorModel.TRANSLUCENT, DataBuffer.TYPE_INT) {
            public int getRed(int pixel) { return (pixel >> 16) & 0xFF; }
            public int getGreen(int pixel) { return (pixel >> 8) & 0xFF; }
            public int getBlue(int pixel) { return pixel & 0xFF; }
            public int getAlpha(int pixel) { return (pixel >>> 24) & 0xFF; }
        };

        check(pcm.getMask(0) == 0x00FF0000, "getMask(0) red");
        check(pcm.getMask(1) == 0x0000FF00, "getMask(1) green");
        check(pcm.getMask(2) == 0x000000FF, "getMask(2) blue");
        check(pcm.getMask(3) == 0xFF000000, "getMask(3) alpha");
        int[] masks = pcm.getMasks();
        check(masks.length == 4 && masks[0] == 0x00FF0000 && masks[3] == 0xFF000000, "getMasks RGB+A");
        check(pcm.getPixelSize() == 32, "pixel size 32");

        SampleModel sm = pcm.createCompatibleSampleModel(4, 4);
        check(sm != null && sm.getWidth() == 4, "createCompatibleSampleModel");
        check(pcm.isCompatibleSampleModel(sm), "isCompatibleSampleModel");

        WritableRaster wr = Raster.createWritableRaster(sm, null);
        WritableRaster alpha = pcm.getAlphaRaster(wr);
        check(alpha != null, "getAlphaRaster non-null");

        PackedColorModel same = new PackedColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), 32,
            new int[]{0x00FF0000, 0x0000FF00, 0x000000FF}, 0xFF000000,
            false, ColorModel.TRANSLUCENT, DataBuffer.TYPE_INT) {
            public int getRed(int pixel) { return (pixel >> 16) & 0xFF; }
            public int getGreen(int pixel) { return (pixel >> 8) & 0xFF; }
            public int getBlue(int pixel) { return pixel & 0xFF; }
            public int getAlpha(int pixel) { return (pixel >>> 24) & 0xFF; }
        };
        check(pcm.equals(same) && pcm.hashCode() == same.hashCode(), "equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += PackedColorModelDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class PackedColorModelDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.PackedColorModel");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.image.ColorModel"), "superclass java.awt.image.ColorModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getMask"), "method getMask");
        check(hasMethod(c, "getMasks"), "method getMasks");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "isCompatibleSampleModel"), "method isCompatibleSampleModel");
        check(hasMethod(c, "getAlphaRaster"), "method getAlphaRaster");
        check(hasMethod(c, "equals"), "method equals");
    }

    /** Minimal concrete subclass to reach the (ColorSpace,int,int[],int,boolean,int,int) constructor. */
    static class TestPacked extends PackedColorModel {
        TestPacked(ColorSpace cs, int bits, int[] colorMaskArray, int alphaMask, boolean isAlphaPremultiplied,
                   int trans, int transferType) {
            super(cs, bits, colorMaskArray, alphaMask, isAlphaPremultiplied, trans, transferType);
        }
        public int getRed(int pixel) { return (pixel >> 16) & 0xff; }
        public int getGreen(int pixel) { return (pixel >> 8) & 0xff; }
        public int getBlue(int pixel) { return pixel & 0xff; }
        public int getAlpha(int pixel) { return 255; }
    }

    static void testMasksViaDirect() {
        // DirectColorModel is the shipped PackedColorModel; masks flow through the packed API.
        DirectColorModel dcm = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
        PackedColorModel pcm = dcm;
        int[] masks = pcm.getMasks();
        check(masks.length == 4, "getMasks length 4");
        check(masks[0] == 0xff0000 && masks[1] == 0xff00 && masks[2] == 0xff && masks[3] == 0xff000000,
              "getMasks exact order r/g/b/a");
        check(pcm.getMask(0) == 0xff0000, "getMask(0) red");
        check(pcm.getMask(1) == 0xff00, "getMask(1) green");
        check(pcm.getMask(2) == 0xff, "getMask(2) blue");
        check(pcm.getMask(3) == 0xff000000, "getMask(3) alpha");
        // Mask indexing follows color components then alpha.
        DirectColorModel d24 = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        check(d24.getMasks().length == 3, "alpha-less masks length 3");
        // createCompatibleSampleModel yields a SinglePixelPackedSampleModel honoring the masks.
        java.awt.image.SampleModel sm = pcm.createCompatibleSampleModel(3, 2);
        check(sm.getWidth() == 3 && sm.getHeight() == 2, "compatible sample model dims");
        check(pcm.isCompatibleSampleModel(sm), "own sample model compatible");
        check(!pcm.isCompatibleSampleModel(new BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR).getSampleModel()),
              "foreign sample model rejected");
        WritableRaster wr = dcm.createCompatibleWritableRaster(2, 2);
        check(pcm.getAlphaRaster(wr) != null, "alpha raster with alpha mask");
        check(d24.getAlphaRaster(d24.createCompatibleWritableRaster(2, 2)) == null,
              "no alpha raster without alpha mask");
    }

    static void testSubclassConstructor() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        TestPacked tp = new TestPacked(cs, 32, new int[] { 0xff0000, 0xff00, 0xff }, 0xff000000, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_INT);
        check(tp.getPixelSize() == 32, "subclass pixel size");
        check(tp.getNumComponents() == 4, "subclass num components");
        check(tp.getNumColorComponents() == 3, "subclass color components");
        check(tp.hasAlpha(), "subclass has alpha");
        check(!tp.isAlphaPremultiplied(), "subclass not premultiplied");
        check(tp.getTransparency() == Transparency.TRANSLUCENT, "subclass transparency");
        check(tp.getRed(0xff112233) == 0x11 && tp.getGreen(0xff112233) == 0x22 && tp.getBlue(0xff112233) == 0x33,
              "subclass component getters");
        // equals across the packed family compares masks.
        DirectColorModel dcm = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
        check(dcm.equals(dcm), "packed equals self");
        check(!dcm.equals(new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0x80000000)), "different alpha mask not equal");
    }



    static int run() throws Exception {
        testWhiteBox();
        testMasksViaDirect();
        testSubclassConstructor();
        return passed;
    }
}
