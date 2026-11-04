/*
 * @test
 * @summary java.awt.Component.BaselineResizeBehavior conformance: white-box structure + black-box behavior.
 * @run main BaselineResizeBehaviorTest
 */

import java.lang.reflect.*;
import java.awt.Component;

public class BaselineResizeBehaviorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Component$BaselineResizeBehavior", false, BaselineResizeBehaviorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Component.BaselineResizeBehavior");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 4, "4 enum constants");
        check(c.getMethod("values") != null, "values() present");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Component.BaselineResizeBehavior[] vals = Component.BaselineResizeBehavior.values();
        check(vals.length == 4, "values().length == 4");
        check(Component.BaselineResizeBehavior.CONSTANT_ASCENT != null, "CONSTANT_ASCENT non-null");
        check(Component.BaselineResizeBehavior.CONSTANT_DESCENT != null, "CONSTANT_DESCENT non-null");
        check(Component.BaselineResizeBehavior.CENTER_OFFSET != null, "CENTER_OFFSET non-null");
        check(Component.BaselineResizeBehavior.OTHER != null, "OTHER non-null");

        check(Component.BaselineResizeBehavior.CONSTANT_ASCENT.ordinal() == 0, "CONSTANT_ASCENT ordinal 0");
        check(Component.BaselineResizeBehavior.CONSTANT_DESCENT.ordinal() == 1, "CONSTANT_DESCENT ordinal 1");
        check(Component.BaselineResizeBehavior.CENTER_OFFSET.ordinal() == 2, "CENTER_OFFSET ordinal 2");
        check(Component.BaselineResizeBehavior.OTHER.ordinal() == 3, "OTHER ordinal 3");

        check(Component.BaselineResizeBehavior.valueOf("CENTER_OFFSET")
                == Component.BaselineResizeBehavior.CENTER_OFFSET, "valueOf(CENTER_OFFSET)");
        check(Component.BaselineResizeBehavior.CONSTANT_ASCENT.name().equals("CONSTANT_ASCENT"), "name()");

        check(vals[0] != vals[1] && vals[1] != vals[2] && vals[2] != vals[3], "constants distinct");
        for (Component.BaselineResizeBehavior v : vals) {
            check(Component.BaselineResizeBehavior.valueOf(v.name()) == v, "valueOf(name) identity");
        }

        try {
            Component.BaselineResizeBehavior.valueOf("BOGUS");
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
