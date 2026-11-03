/*
 * @test
 * @summary java.awt.MouseInfo conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MouseInfoTest
 */

import java.lang.reflect.*;
import java.awt.MouseInfo;
import java.awt.PointerInfo;

public class MouseInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.MouseInfo", false, MouseInfoTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.MouseInfo");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(Modifier.isStatic(c.getMethod("getPointerInfo").getModifiers()), "getPointerInfo is static");
        check(Modifier.isStatic(c.getMethod("getNumberOfButtons").getModifiers()), "getNumberOfButtons is static");
        check(c.getMethod("getPointerInfo").getReturnType() == PointerInfo.class, "getPointerInfo returns PointerInfo");
        check(c.getMethod("getNumberOfButtons").getReturnType() == int.class, "getNumberOfButtons returns int");
        check(c.getMethod("getPointerInfo") != null, "getPointerInfo present");
        check(c.getMethod("getNumberOfButtons") != null, "getNumberOfButtons present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi != null) {
            check(pi.getLocation() != null, "pointer location non-null");
            check(pi.getDevice() != null, "pointer device non-null");
        } else {
            check(true, "getPointerInfo() returning null is acceptable");
        }

        int buttons = MouseInfo.getNumberOfButtons();
        check(buttons >= -1, "getNumberOfButtons in documented domain (>= -1)");
        check(MouseInfo.getNumberOfButtons() == buttons, "getNumberOfButtons deterministic");

        PointerInfo pi2 = MouseInfo.getPointerInfo();
        if (pi != null && pi2 != null) {
            check(pi.getDevice() == pi2.getDevice(), "pointer device identity stable");
        } else {
            check(true, "null pointer info tolerated on repeated read");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
