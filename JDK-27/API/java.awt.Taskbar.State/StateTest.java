/*
 * @test
 * @summary java.awt.Taskbar.State conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main StateTest
 */

import java.lang.reflect.*;
import java.awt.Taskbar;

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Taskbar$State", false, StateTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Taskbar.State");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 5, "5 enum constants");
        check(c.getMethod("values") != null, "values() present");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Taskbar.State[] vals = Taskbar.State.values();
        check(vals.length == 5, "values().length == 5");
        check(Taskbar.State.OFF.ordinal() == 0, "OFF ordinal 0");
        check(Taskbar.State.NORMAL.ordinal() == 1, "NORMAL ordinal 1");
        check(Taskbar.State.PAUSED.ordinal() == 2, "PAUSED ordinal 2");
        check(Taskbar.State.INDETERMINATE.ordinal() == 3, "INDETERMINATE ordinal 3");
        check(Taskbar.State.ERROR.ordinal() == 4, "ERROR ordinal 4");

        check(Taskbar.State.valueOf("ERROR") == Taskbar.State.ERROR, "valueOf(ERROR)");
        check(Taskbar.State.NORMAL.name().equals("NORMAL"), "name()");

        boolean distinct = true;
        for (int i = 0; i < vals.length; i++)
            for (int j = i + 1; j < vals.length; j++)
                distinct &= (vals[i] != vals[j]);
        check(distinct, "all 5 constants distinct");

        for (Taskbar.State s : vals) {
            check(Taskbar.State.valueOf(s.name()) == s, "valueOf(name) identity for " + s);
        }

        try {
            Taskbar.State.valueOf("BOGUS");
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
