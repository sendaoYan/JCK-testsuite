/*
 * @test
 * @summary java.awt.TrayIcon.MessageType conformance: white-box structure + black-box behavior.
 * @run main MessageTypeTest
 */

import java.lang.reflect.*;
import java.awt.TrayIcon;

public class MessageTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.TrayIcon$MessageType", false, MessageTypeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.TrayIcon.MessageType");
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
        TrayIcon.MessageType[] vals = TrayIcon.MessageType.values();
        check(vals.length == 4, "values().length == 4");
        check(TrayIcon.MessageType.ERROR != null, "ERROR non-null");
        check(TrayIcon.MessageType.WARNING != null, "WARNING non-null");
        check(TrayIcon.MessageType.INFO != null, "INFO non-null");
        check(TrayIcon.MessageType.NONE != null, "NONE non-null");

        // declaration order
        check(TrayIcon.MessageType.ERROR.ordinal() == 0, "ERROR ordinal 0");
        check(TrayIcon.MessageType.WARNING.ordinal() == 1, "WARNING ordinal 1");
        check(TrayIcon.MessageType.INFO.ordinal() == 2, "INFO ordinal 2");
        check(TrayIcon.MessageType.NONE.ordinal() == 3, "NONE ordinal 3");

        check(TrayIcon.MessageType.ERROR.name().equals("ERROR"), "ERROR name");
        check(TrayIcon.MessageType.valueOf("ERROR") == TrayIcon.MessageType.ERROR, "valueOf(ERROR)");
        check(TrayIcon.MessageType.valueOf("NONE") == TrayIcon.MessageType.NONE, "valueOf(NONE)");

        // distinct values + round-trip through values()/valueOf()
        check(vals[0] != vals[1] && vals[1] != vals[2] && vals[2] != vals[3], "constants distinct");
        for (TrayIcon.MessageType t : vals) {
            check(TrayIcon.MessageType.valueOf(t.name()) == t, "valueOf(name) identity for " + t);
        }

        try {
            TrayIcon.MessageType.valueOf("BOGUS");
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
