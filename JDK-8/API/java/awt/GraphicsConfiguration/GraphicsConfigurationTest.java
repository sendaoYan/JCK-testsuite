/*
 * @test
 * @summary java.awt.GraphicsConfiguration conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main GraphicsConfigurationTest
 */

import java.awt.*;
import java.awt.image.*;

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

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsConfiguration");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDevice"), "method getDevice");
        check(hasMethod(c, "createCompatibleImage"), "method createCompatibleImage");
        check(hasMethod(c, "createCompatibleVolatileImage"), "method createCompatibleVolatileImage");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getDefaultTransform"), "method getDefaultTransform");
        check(hasMethod(c, "getNormalizingTransform"), "method getNormalizingTransform");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getBufferCapabilities"), "method getBufferCapabilities");
        check(hasMethod(c, "getImageCapabilities"), "method getImageCapabilities");
        check(hasMethod(c, "isTranslucencyCapable"), "method isTranslucencyCapable");
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
        checkIAE(() -> gc.createCompatibleImage(0, 0), "zero dims");

        if (gc.getColorModel(Transparency.TRANSLUCENT) != null) {
            BufferedImage tl = gc.createCompatibleImage(8, 8, Transparency.TRANSLUCENT);
            check(tl != null, "createCompatibleImage(TRANSLUCENT) non-null when supported");
        }

        // every configuration in the device satisfies the same invariants
        // (bounded to the first 10: X11 devices may expose hundreds of visuals)
        GraphicsConfiguration[] allCfg = device.getConfigurations();
        int limit = Math.min(10, allCfg.length);
        for (int i = 0; i < limit; i++) {
            GraphicsConfiguration cfg = allCfg[i];
            check(cfg.getDevice() == device, "config device matches");
            Rectangle cb = cfg.getBounds();
            check(cb != null && cb.width > 0 && cb.height > 0, "config bounds positive size");
            check(cfg.getColorModel() != null, "config color model non-null");
            check(cfg.getDefaultTransform() != null, "config default transform non-null");
            check(cfg.getNormalizingTransform() != null, "config normalizing transform non-null");
        }

        // volatile image factory: dims exact, invalid dims rejected
        VolatileImage vi = gc.createCompatibleVolatileImage(8, 8);
        check(vi != null && vi.getWidth() == 8 && vi.getHeight() == 8,
              "createCompatibleVolatileImage(8,8) dims");
        VolatileImage vit = gc.createCompatibleVolatileImage(8, 8, Transparency.TRANSLUCENT);
        check(vit != null && vit.getWidth() == 8, "createCompatibleVolatileImage translucency overload");
        checkIAE(() -> gc.createCompatibleVolatileImage(-1, 8), "volatile negative width");
        checkIAE(() -> gc.createCompatibleVolatileImage(8, 0), "volatile zero height");

        // isTranslucencyCapable returns a stable boolean (value is platform-dependent)
        boolean t1 = gc.isTranslucencyCapable();
        check(t1 == gc.isTranslucencyCapable(), "isTranslucencyCapable stable");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
