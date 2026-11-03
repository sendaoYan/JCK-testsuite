/*
 * @test
 * @summary java.awt.Cursor conformance: white-box structure + black-box behavior.
 * @run main CursorTest
 */

import java.awt.Cursor;
import java.lang.reflect.*;
import java.io.Serializable;
import java.util.*;

public class CursorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + msg);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Cursor", false, CursorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Cursor");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(Serializable.class.isAssignableFrom(c), "implements java.io.Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");

        check(c.getConstructor(int.class) != null, "ctor (int)");

        for (String name : new String[] {"getType", "getName", "toString", "getPredefinedCursor",
                "getDefaultCursor", "getSystemCustomCursor"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);

        String[] consts = {"DEFAULT_CURSOR", "CROSSHAIR_CURSOR", "TEXT_CURSOR", "WAIT_CURSOR",
                "SW_RESIZE_CURSOR", "SE_RESIZE_CURSOR", "NW_RESIZE_CURSOR", "NE_RESIZE_CURSOR",
                "N_RESIZE_CURSOR", "S_RESIZE_CURSOR", "W_RESIZE_CURSOR", "E_RESIZE_CURSOR",
                "HAND_CURSOR", "MOVE_CURSOR", "CUSTOM_CURSOR"};
        for (String name : consts) {
            Field f = c.getField(name);
            check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                  && f.getType() == int.class, "public static final int " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constant values (stable API contract)
        check(Cursor.DEFAULT_CURSOR == 0, "DEFAULT_CURSOR == 0");
        check(Cursor.CROSSHAIR_CURSOR == 1, "CROSSHAIR_CURSOR == 1");
        check(Cursor.TEXT_CURSOR == 2, "TEXT_CURSOR == 2");
        check(Cursor.WAIT_CURSOR == 3, "WAIT_CURSOR == 3");
        check(Cursor.SW_RESIZE_CURSOR == 4, "SW_RESIZE_CURSOR == 4");
        check(Cursor.SE_RESIZE_CURSOR == 5, "SE_RESIZE_CURSOR == 5");
        check(Cursor.NW_RESIZE_CURSOR == 6, "NW_RESIZE_CURSOR == 6");
        check(Cursor.NE_RESIZE_CURSOR == 7, "NE_RESIZE_CURSOR == 7");
        check(Cursor.N_RESIZE_CURSOR == 8, "N_RESIZE_CURSOR == 8");
        check(Cursor.S_RESIZE_CURSOR == 9, "S_RESIZE_CURSOR == 9");
        check(Cursor.W_RESIZE_CURSOR == 10, "W_RESIZE_CURSOR == 10");
        check(Cursor.E_RESIZE_CURSOR == 11, "E_RESIZE_CURSOR == 11");
        check(Cursor.HAND_CURSOR == 12, "HAND_CURSOR == 12");
        check(Cursor.MOVE_CURSOR == 13, "MOVE_CURSOR == 13");
        check(Cursor.CUSTOM_CURSOR == -1, "CUSTOM_CURSOR == -1");

        // constructor + getType
        check(new Cursor(Cursor.DEFAULT_CURSOR).getType() == Cursor.DEFAULT_CURSOR,
              "new Cursor(DEFAULT).getType()");
        check(new Cursor(Cursor.HAND_CURSOR).getType() == Cursor.HAND_CURSOR,
              "new Cursor(HAND).getType()");
        check(new Cursor(Cursor.MOVE_CURSOR).getType() == Cursor.MOVE_CURSOR,
              "new Cursor(MOVE).getType()");
        check(new Cursor(Cursor.CROSSHAIR_CURSOR).getType() == Cursor.CROSSHAIR_CURSOR,
              "new Cursor(CROSSHAIR).getType()");

        // getName / toString non-empty + consistent
        Cursor c0 = new Cursor(Cursor.DEFAULT_CURSOR);
        check(c0.getName() != null && !c0.getName().isEmpty(), "getName non-empty");
        check(c0.toString().startsWith("java.awt.Cursor["), "toString prefix");
        check(c0.toString().contains(c0.getName()), "toString contains name");
        check(c0.toString().endsWith("]"), "toString suffix");

        // getPredefinedCursor consistency + caching
        for (int t = 0; t <= Cursor.MOVE_CURSOR; t++) {
            final int type = t;
            check(Cursor.getPredefinedCursor(type).getType() == type,
                  "getPredefinedCursor(" + type + ").getType()");
        }
        check(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
              == Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
              "getPredefinedCursor caches same instance");

        // getDefaultCursor
        check(Cursor.getDefaultCursor().getType() == Cursor.DEFAULT_CURSOR,
              "getDefaultCursor().getType() == DEFAULT_CURSOR");
        check(Cursor.getDefaultCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),
              "getDefaultCursor == getPredefinedCursor(DEFAULT)");
        check(Cursor.getDefaultCursor().getName().equals(new Cursor(Cursor.DEFAULT_CURSOR).getName()),
              "default cursor name consistent with new Cursor(0)");

        // new Cursor creates a fresh (non-cached) instance
        check(new Cursor(Cursor.DEFAULT_CURSOR) != Cursor.getDefaultCursor(),
              "new Cursor is a distinct instance from cached default");

        // invalid types -> IllegalArgumentException
        checkIAE(() -> new Cursor(Cursor.CUSTOM_CURSOR), "new Cursor(CUSTOM_CURSOR=-1)");
        checkIAE(() -> new Cursor(-1), "new Cursor(-1)");
        checkIAE(() -> new Cursor(14), "new Cursor(14)");
        checkIAE(() -> new Cursor(100), "new Cursor(100)");
        checkIAE(() -> Cursor.getPredefinedCursor(Cursor.CUSTOM_CURSOR), "getPredefinedCursor(-1)");
        checkIAE(() -> Cursor.getPredefinedCursor(14), "getPredefinedCursor(14)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
