/*
 * @test
 * @summary java.awt.Transparency conformance: white-box structure + black-box behavior.
 * @run main TransparencyTest
 */

import java.lang.reflect.*;
import java.awt.Transparency;

public class TransparencyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Transparency", false, TransparencyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Transparency");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isInterface(m), "public interface");
        check(c.isInterface(), "is interface");
        check(c.getField("OPAQUE").getType() == int.class, "int OPAQUE");
        check(c.getField("BITMASK").getType() == int.class, "int BITMASK");
        check(c.getField("TRANSLUCENT").getType() == int.class, "int TRANSLUCENT");
        check(c.getMethod("getTransparency") != null, "getTransparency() present");
        check(c.getMethod("getTransparency").getReturnType() == int.class, "getTransparency returns int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(Transparency.OPAQUE == 1, "OPAQUE == 1");
        check(Transparency.BITMASK == 2, "BITMASK == 2");
        check(Transparency.TRANSLUCENT == 3, "TRANSLUCENT == 3");
        check(Transparency.OPAQUE != Transparency.BITMASK && Transparency.BITMASK != Transparency.TRANSLUCENT,
              "constants distinct");

        Transparency opaque = new Transparency() { public int getTransparency() { return Transparency.OPAQUE; } };
        Transparency translucent = new Transparency() { public int getTransparency() { return Transparency.TRANSLUCENT; } };
        check(opaque.getTransparency() == Transparency.OPAQUE, "impl returns OPAQUE");
        check(translucent.getTransparency() == Transparency.TRANSLUCENT, "impl returns TRANSLUCENT");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Transparency.class.isInterface(), "Transparency is an interface");
        check(java.awt.Transparency.OPAQUE == 1, "OPAQUE == 1");
        check(java.awt.Transparency.BITMASK == 2, "BITMASK == 2");
        check(java.awt.Transparency.TRANSLUCENT == 3, "TRANSLUCENT == 3");
        // implementors return the right values
        check(java.awt.Color.red.getTransparency() == java.awt.Transparency.OPAQUE, "Color implements Transparency: red OPAQUE");
        check(new java.awt.Color(255, 0, 0, 128).getTransparency() == java.awt.Transparency.TRANSLUCENT,
              "translucent red");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).getTransparency()
              == java.awt.Transparency.TRANSLUCENT, "ARGB image TRANSLUCENT");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB).getTransparency()
              == java.awt.Transparency.OPAQUE, "RGB image OPAQUE");
        check(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).getColorModel()
              .getTransparency() == java.awt.Transparency.TRANSLUCENT, "ARGB color model TRANSLUCENT");
    }

}
