/*
 * @test
 * @summary java.awt.Desktop.Action conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ActionTest
 */

import java.lang.reflect.*;
import java.awt.Desktop;

public class ActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Desktop$Action", false, ActionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Desktop.Action");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 24, "24 enum constants");
        check(c.getMethod("values") != null, "values() present");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Desktop.Action[] vals = Desktop.Action.values();
        check(vals.length == 24, "values().length == 24");

        check(Desktop.Action.OPEN.ordinal() == 0, "OPEN ordinal 0");
        check(Desktop.Action.EDIT.ordinal() == 1, "EDIT ordinal 1");
        check(Desktop.Action.PRINT.ordinal() == 2, "PRINT ordinal 2");
        check(Desktop.Action.MAIL.ordinal() == 3, "MAIL ordinal 3");
        check(Desktop.Action.BROWSE.ordinal() == 4, "BROWSE ordinal 4");

        check(Desktop.Action.MOVE_TO_TRASH != null, "MOVE_TO_TRASH non-null");
        check(Desktop.Action.APP_OPEN_FILE != null, "APP_OPEN_FILE non-null");
        check(Desktop.Action.APP_QUIT_HANDLER != null, "APP_QUIT_HANDLER non-null");

        check(Desktop.Action.valueOf("OPEN") == Desktop.Action.OPEN, "valueOf(OPEN)");
        check(Desktop.Action.valueOf("BROWSE_FILE_DIR") == Desktop.Action.BROWSE_FILE_DIR, "valueOf(BROWSE_FILE_DIR)");
        check(Desktop.Action.MAIL.name().equals("MAIL"), "name()");

        boolean distinct = true;
        for (int i = 0; i < vals.length; i++) {
            for (int j = i + 1; j < vals.length; j++) {
                distinct &= (vals[i] != vals[j]);
            }
        }
        check(distinct, "all 24 constants distinct");

        for (Desktop.Action a : vals) {
            check(Desktop.Action.valueOf(a.name()) == a, "valueOf(name) identity for " + a);
        }

        try {
            Desktop.Action.valueOf("BOGUS");
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
