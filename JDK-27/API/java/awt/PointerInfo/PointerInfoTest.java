/*
 * @test
 * @summary java.awt.PointerInfo conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PointerInfoTest
 */

import java.lang.reflect.*;
import java.awt.MouseInfo;
import java.awt.PointerInfo;

public class PointerInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.PointerInfo", false, PointerInfoTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.PointerInfo");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getDeclaredConstructors().length == 1, "single package-private constructor");
        check(c.getMethod("getDevice").getReturnType() == java.awt.GraphicsDevice.class, "getDevice returns GraphicsDevice");
        check(c.getMethod("getLocation").getReturnType() == java.awt.Point.class, "getLocation returns Point");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi != null) {
            java.awt.GraphicsDevice dev = pi.getDevice();
            java.awt.Point loc = pi.getLocation();
            check(dev != null, "getDevice non-null");
            check(loc != null, "getLocation non-null");
            check(loc instanceof java.awt.Point, "location is a java.awt.Point");
            // repeated reads are consistent
            check(pi.getDevice() == dev, "getDevice deterministic");
        } else {
            check(true, "getPointerInfo() returning null is acceptable");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
