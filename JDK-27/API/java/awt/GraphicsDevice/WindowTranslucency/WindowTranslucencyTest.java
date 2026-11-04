/*
 * @test
 * @summary java.awt.GraphicsDevice.WindowTranslucency conformance: white-box structure + black-box behavior.
 * @run main WindowTranslucencyTest
 */

import java.lang.reflect.*;
import java.awt.GraphicsDevice;

public class WindowTranslucencyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsDevice$WindowTranslucency", false, WindowTranslucencyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GraphicsDevice.WindowTranslucency");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 3, "3 enum constants");
        check(c.getMethod("values") != null, "values() present");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        GraphicsDevice.WindowTranslucency[] vals = GraphicsDevice.WindowTranslucency.values();
        check(vals.length == 3, "values().length == 3");
        check(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT != null, "PERPIXEL_TRANSPARENT non-null");
        check(GraphicsDevice.WindowTranslucency.TRANSLUCENT != null, "TRANSLUCENT non-null");
        check(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT != null, "PERPIXEL_TRANSLUCENT non-null");

        check(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT.ordinal() == 0, "PERPIXEL_TRANSPARENT ordinal 0");
        check(GraphicsDevice.WindowTranslucency.TRANSLUCENT.ordinal() == 1, "TRANSLUCENT ordinal 1");
        check(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT.ordinal() == 2, "PERPIXEL_TRANSLUCENT ordinal 2");

        check(GraphicsDevice.WindowTranslucency.valueOf("TRANSLUCENT")
                == GraphicsDevice.WindowTranslucency.TRANSLUCENT, "valueOf(TRANSLUCENT)");
        check(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT.name().equals("PERPIXEL_TRANSLUCENT"),
              "name() roundtrip");

        check(vals[0] != vals[1] && vals[1] != vals[2], "constants distinct");
        for (GraphicsDevice.WindowTranslucency t : vals) {
            check(GraphicsDevice.WindowTranslucency.valueOf(t.name()) == t, "valueOf(name) identity");
        }

        try {
            GraphicsDevice.WindowTranslucency.valueOf("BOGUS");
            check(false, "valueOf unknown should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown throws IllegalArgumentException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
