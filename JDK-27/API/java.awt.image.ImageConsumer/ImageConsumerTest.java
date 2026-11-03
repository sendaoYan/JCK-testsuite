/*
 * @test
 * @summary java.awt.image.ImageConsumer conformance: white-box structure + constant values.
 * @run main ImageConsumerTest
 */

import java.awt.image.ImageConsumer;
import java.lang.reflect.*;

public class ImageConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }
    static int fieldInt(Class<?> c, String name) {
        try { return c.getField(name).getInt(null); } catch (Exception e) { throw new AssertionError(name + ": " + e); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImageConsumer", false, ImageConsumerTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "setDimensions", int.class, int.class), "setDimensions");
        check(hasMethod(c, "setProperties", java.util.Hashtable.class), "setProperties");
        check(hasMethod(c, "setColorModel", java.awt.image.ColorModel.class), "setColorModel");
        check(hasMethod(c, "setHints", int.class), "setHints");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, java.awt.image.ColorModel.class, byte[].class, int.class, int.class), "setPixels byte[]");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, java.awt.image.ColorModel.class, int[].class, int.class, int.class), "setPixels int[]");
        check(hasMethod(c, "imageComplete", int.class), "imageComplete");
    }

    static void testBlackBox() {
        check(fieldInt(ImageConsumer.class, "RANDOMPIXELORDER") == 1, "RANDOMPIXELORDER 1");
        check(fieldInt(ImageConsumer.class, "TOPDOWNLEFTRIGHT") == 2, "TOPDOWNLEFTRIGHT 2");
        check(fieldInt(ImageConsumer.class, "COMPLETESCANLINES") == 4, "COMPLETESCANLINES 4");
        check(fieldInt(ImageConsumer.class, "SINGLEPASS") == 8, "SINGLEPASS 8");
        check(fieldInt(ImageConsumer.class, "SINGLEFRAME") == 16, "SINGLEFRAME 16");
        check(fieldInt(ImageConsumer.class, "IMAGEERROR") == 1, "IMAGEERROR 1");
        check(fieldInt(ImageConsumer.class, "SINGLEFRAMEDONE") == 2, "SINGLEFRAMEDONE 2");
        check(fieldInt(ImageConsumer.class, "STATICIMAGEDONE") == 3, "STATICIMAGEDONE 3");
        check(fieldInt(ImageConsumer.class, "IMAGEABORTED") == 4, "IMAGEABORTED 4");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
