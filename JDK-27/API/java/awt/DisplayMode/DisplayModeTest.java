/*
 * @test
 * @summary java.awt.DisplayMode conformance: white-box structure + black-box behavior.
 * @run main DisplayModeTest
 */

import java.lang.reflect.*;
import java.awt.DisplayMode;

public class DisplayModeTest {
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
        Class<?> c = Class.forName("java.awt.DisplayMode", false, DisplayModeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.DisplayMode");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(int.class, int.class, int.class, int.class) != null, "ctor (int,int,int,int)");
        for (String n : new String[] {"getWidth", "getHeight", "getBitDepth", "getRefreshRate",
                "equals", "hashCode", "toString"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(DisplayMode.BIT_DEPTH_MULTI == -1, "BIT_DEPTH_MULTI == -1");
        check(DisplayMode.REFRESH_RATE_UNKNOWN == 0, "REFRESH_RATE_UNKNOWN == 0");

        DisplayMode dm = new DisplayMode(1920, 1080, 32, 60);
        check(dm.getWidth() == 1920, "getWidth 1920");
        check(dm.getHeight() == 1080, "getHeight 1080");
        check(dm.getBitDepth() == 32, "getBitDepth 32");
        check(dm.getRefreshRate() == 60, "getRefreshRate 60");

        DisplayMode same = new DisplayMode(1920, 1080, 32, 60);
        DisplayMode diff = new DisplayMode(1280, 720, 24, 50);
        check(dm.equals(same), "equals same values");
        check(same.equals(dm), "equals symmetric");
        check(dm.hashCode() == same.hashCode(), "equal values equal hashCode");
        check(!dm.equals(diff), "not equal different values");
        check(!dm.equals(null), "not equal null");
        check(!dm.equals("1920x1080x32bpp@60Hz"), "not equal unrelated type");

        check(dm.hashCode() == 1920 + 1080 + 32 * 7 + 60 * 13, "hashCode formula");
        check(dm.toString().equals("1920x1080x32bpp@60Hz"), "toString format: " + dm);

        DisplayMode multi = new DisplayMode(1024, 768, DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN);
        check(multi.getBitDepth() == -1 && multi.getRefreshRate() == 0, "multi/unknown roundtrip");
        check(multi.toString().equals("1024x768x[Multi depth]@[Unknown refresh rate]"),
              "toString multi format: " + multi);
        check(multi.hashCode() == 1024 + 768 + (-1) * 7 + 0 * 13, "multi hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
