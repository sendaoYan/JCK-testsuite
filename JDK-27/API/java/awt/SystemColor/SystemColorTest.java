/*
 * @test
 * @summary java.awt.SystemColor conformance: white-box structure + black-box behavior.
 * @run main SystemColorTest
 */

import java.lang.reflect.*;
import java.awt.SystemColor;

public class SystemColorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.SystemColor", false, SystemColorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.SystemColor");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getSuperclass() == java.awt.Color.class, "extends java.awt.Color");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getField("DESKTOP").getType() == int.class, "public static final int DESKTOP");
        check(c.getField("NUM_COLORS").getType() == int.class, "public static final int NUM_COLORS");
        check(c.getField("window").getType() == SystemColor.class, "public static final SystemColor window");
        check(c.getField("desktop").getType() == SystemColor.class, "public static final SystemColor desktop");
        check(c.getField("infoText").getType() == SystemColor.class, "public static final SystemColor infoText");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // fixed color-index constants
        check(SystemColor.DESKTOP == 0, "DESKTOP == 0");
        check(SystemColor.WINDOW == 7, "WINDOW == 7");
        check(SystemColor.INFO == 24, "INFO == 24");
        check(SystemColor.INFO_TEXT == 25, "INFO_TEXT == 25");
        check(SystemColor.NUM_COLORS == 26, "NUM_COLORS == 26");

        // canonical instances are real colors
        SystemColor[] samples = { SystemColor.desktop, SystemColor.window, SystemColor.windowText,
                SystemColor.control, SystemColor.menu, SystemColor.info, SystemColor.infoText };
        for (SystemColor sc : samples) {
            check(sc instanceof java.awt.Color, "instanceof java.awt.Color");
        }

        // opaque, in-range RGB channels (exact int, theme-independent)
        int rgb = SystemColor.window.getRGB();
        check((rgb >>> 24) == 0xFF, "alpha byte == 0xFF");
        check(SystemColor.window.getAlpha() == 255, "getAlpha() == 255");
        check(SystemColor.window.getRed() >= 0 && SystemColor.window.getRed() <= 255, "red in [0,255]");
        check(SystemColor.window.getGreen() >= 0 && SystemColor.window.getGreen() <= 255, "green in [0,255]");
        check(SystemColor.window.getBlue() >= 0 && SystemColor.window.getBlue() <= 255, "blue in [0,255]");

        // getRGB composes alpha+red+green+blue
        int r = SystemColor.window.getRed(), g = SystemColor.window.getGreen(), b = SystemColor.window.getBlue();
        check(SystemColor.window.getRGB() == ((0xFF << 24) | (r << 16) | (g << 8) | b),
              "getRGB == (alpha,red,green,blue) composed");

        // deterministic within process
        check(SystemColor.window.getRGB() == SystemColor.window.getRGB(), "getRGB deterministic");

        // toString reports class + color index
        check(SystemColor.window.toString().equals("java.awt.SystemColor[i=7]"),
              "toString == java.awt.SystemColor[i=7], got " + SystemColor.window.toString());
        check(SystemColor.desktop.toString().equals("java.awt.SystemColor[i=0]"),
              "desktop toString index 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
