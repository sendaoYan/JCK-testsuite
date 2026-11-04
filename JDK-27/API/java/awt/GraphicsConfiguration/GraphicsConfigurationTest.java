/*
 * @test
 * @summary java.awt.GraphicsConfiguration conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main GraphicsConfigurationTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.util.*;

public class GraphicsConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + msg);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsConfiguration", false,
                                   GraphicsConfigurationTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GraphicsConfiguration");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");

        for (String name : new String[] {"getDevice", "createCompatibleImage",
                "createCompatibleVolatileImage", "getColorModel", "getDefaultTransform",
                "getNormalizingTransform", "getBounds", "getBufferCapabilities",
                "getImageCapabilities", "isTranslucencyCapable"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);

        check(Modifier.isAbstract(c.getMethod("getDevice").getModifiers()),
              "getDevice abstract");
        check(Modifier.isAbstract(c.getMethod("getColorModel").getModifiers()),
              "getColorModel abstract");
        check(Modifier.isAbstract(c.getMethod("getBounds").getModifiers()),
              "getBounds abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        check(gc != null, "default configuration non-null");

        check(gc.getDevice() == device, "getDevice returns owning device");

        Rectangle bounds = gc.getBounds();
        check(bounds != null, "getBounds non-null");
        check(bounds.width > 0 && bounds.height > 0, "bounds positive size");

        ColorModel cm = gc.getColorModel();
        check(cm != null, "getColorModel non-null");
        check(gc.getColorModel(Transparency.OPAQUE) != null,
              "getColorModel(OPAQUE) non-null");

        check(gc.getDefaultTransform() != null, "getDefaultTransform non-null");
        check(gc.getNormalizingTransform() != null, "getNormalizingTransform non-null");

        BufferCapabilities bc = gc.getBufferCapabilities();
        check(bc != null, "getBufferCapabilities non-null");
        check(bc.getFrontBufferCapabilities() != null, "front buffer capabilities non-null");
        check(bc.getBackBufferCapabilities() != null, "back buffer capabilities non-null");

        check(gc.getImageCapabilities() != null, "getImageCapabilities non-null");
        gc.isTranslucencyCapable(); // boolean, value platform-dependent

        // createCompatibleImage + pixel round-trip
        BufferedImage img = gc.createCompatibleImage(8, 8);
        check(img != null, "createCompatibleImage non-null");
        check(img.getWidth() == 8 && img.getHeight() == 8, "compatible image dims 8x8");
        img.setRGB(0, 0, 0xFF000000);
        img.setRGB(1, 1, 0xFFFFFFFF);
        check(img.getRGB(0, 0) == 0xFF000000, "black pixel round-trips");
        check(img.getRGB(1, 1) == 0xFFFFFFFF, "white pixel round-trips");

        BufferedImage opaque = gc.createCompatibleImage(8, 8, Transparency.OPAQUE);
        check(opaque != null, "createCompatibleImage(OPAQUE) non-null");
        check(opaque.getWidth() == 8 && opaque.getHeight() == 8,
              "opaque compatible image dims 8x8");

        checkIAE(() -> gc.createCompatibleImage(-1, 8), "negative width");
        checkIAE(() -> gc.createCompatibleImage(8, -1), "negative height");
        checkIAE(() -> gc.createCompatibleImage(0, 8), "zero width");

        if (gc.getColorModel(Transparency.TRANSLUCENT) != null) {
            BufferedImage tl = gc.createCompatibleImage(8, 8, Transparency.TRANSLUCENT);
            check(tl != null, "createCompatibleImage(TRANSLUCENT) non-null when supported");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
