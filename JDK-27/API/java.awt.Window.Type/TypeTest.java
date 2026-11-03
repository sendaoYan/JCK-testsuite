/*
 * @test
 * @summary java.awt.Window.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.*;

public class TypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Window$Type");
        check(c != null, "type loads: java.awt.Window.Type");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants().length == 3, "three constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.Window.Type[] all = java.awt.Window.Type.values();
        check(all.length == 3, "values length 3");
        check(all[0] == java.awt.Window.Type.NORMAL, "values[0] NORMAL");
        check(all[1] == java.awt.Window.Type.UTILITY, "values[1] UTILITY");
        check(all[2] == java.awt.Window.Type.POPUP, "values[2] POPUP");

        check(java.awt.Window.Type.NORMAL.ordinal() == 0, "NORMAL ordinal 0");
        check(java.awt.Window.Type.POPUP.ordinal() == 2, "POPUP ordinal 2");

        for (java.awt.Window.Type t : all) {
            check(java.awt.Window.Type.valueOf(t.name()) == t, "valueOf roundtrip " + t);
        }
        check(java.awt.Window.Type.valueOf("UTILITY") == java.awt.Window.Type.UTILITY, "valueOf literal");
        check(java.awt.Window.Type.NORMAL.toString().equals("NORMAL"), "toString = name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
