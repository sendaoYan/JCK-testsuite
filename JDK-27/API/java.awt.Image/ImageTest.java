/*
 * @test
 * @summary java.awt.Image conformance: white-box structure + black-box behavior.
 * @run main ImageTest
 */

import java.lang.reflect.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class ImageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Image", false, ImageTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Image");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        for (String n : new String[] {"getWidth", "getHeight", "getSource", "getGraphics",
                "getProperty", "getScaledInstance", "flush", "getCapabilities",
                "setAccelerationPriority", "getAccelerationPriority"})
            check(hasMethod(c, n), "method present: " + n);
        check(c.getField("UndefinedProperty") != null, "UndefinedProperty field present");
        check(c.getField("SCALE_DEFAULT") != null, "SCALE_DEFAULT field present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // scaling-hint constants
        check(Image.SCALE_DEFAULT == 1, "SCALE_DEFAULT == 1");
        check(Image.SCALE_FAST == 2, "SCALE_FAST == 2");
        check(Image.SCALE_SMOOTH == 4, "SCALE_SMOOTH == 4");
        check(Image.SCALE_REPLICATE == 8, "SCALE_REPLICATE == 8");
        check(Image.SCALE_AREA_AVERAGING == 16, "SCALE_AREA_AVERAGING == 16");
        check(Image.UndefinedProperty != null, "UndefinedProperty non-null");

        BufferedImage img = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
        check(img instanceof Image, "BufferedImage is an Image");
        check(img.getWidth(null) == 10, "getWidth(null) == 10");
        check(img.getHeight(null) == 20, "getHeight(null) == 20");

        // acceleration priority: default + round-trip + boundary
        check(img.getAccelerationPriority() == 0.5f, "default acceleration priority == 0.5");
        img.setAccelerationPriority(0.75f);
        check(img.getAccelerationPriority() == 0.75f, "set/get acceleration priority");
        img.setAccelerationPriority(0.0f);
        check(img.getAccelerationPriority() == 0.0f, "priority 0.0 boundary");
        img.setAccelerationPriority(1.0f);
        check(img.getAccelerationPriority() == 1.0f, "priority 1.0 boundary");
        try { img.setAccelerationPriority(-0.1f); check(false, "negative priority should throw"); }
        catch (IllegalArgumentException expected) { check(true, "IllegalArgumentException for priority < 0"); }
        try { img.setAccelerationPriority(1.1f); check(false, "priority > 1 should throw"); }
        catch (IllegalArgumentException expected) { check(true, "IllegalArgumentException for priority > 1"); }

        // capabilities / source / property
        check(img.getCapabilities(null) != null, "getCapabilities(null) non-null");
        check(img.getSource() != null, "getSource non-null");
        check(img.getProperty("no-such-property", null) == Image.UndefinedProperty,
              "unknown property == UndefinedProperty");

        // scaled instance
        Image scaled = img.getScaledInstance(5, 10, Image.SCALE_SMOOTH);
        check(scaled != null, "getScaledInstance non-null");
        check(scaled != img, "getScaledInstance returns a distinct image");
        check(scaled.getWidth(null) == 5 && scaled.getHeight(null) == 10, "scaled dimensions 5x10");
        try { img.getScaledInstance(0, 10, Image.SCALE_DEFAULT); check(false, "zero width should throw"); }
        catch (IllegalArgumentException expected) { check(true, "IllegalArgumentException for zero width"); }

        // flush no-throw
        img.flush();
        check(true, "flush() callable");

        // "not yet loaded" contract: unknown dimensions report -1
        Image unloaded = new Image() {
            public int getWidth(ImageObserver o) { return -1; }
            public int getHeight(ImageObserver o) { return -1; }
            public ImageProducer getSource() { return null; }
            public Graphics getGraphics() { return null; }
            public Object getProperty(String name, ImageObserver o) { return null; }
        };
        check(unloaded.getWidth(null) == -1, "unloaded getWidth(null) == -1");
        check(unloaded.getHeight(null) == -1, "unloaded getHeight(null) == -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
