/*
 * @test
 * @summary java.awt.image.PackedColorModel conformance: white-box structure plus black-box behavior.
 * @run main PackedColorModelTest
 */

import java.awt.color.*;
import java.awt.image.*;
import java.lang.reflect.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
