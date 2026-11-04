/*
 * @test
 * @summary javax.accessibility.AccessibleState conformance: white-box structure + black-box behavior.
 * @run main AccessibleStateTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class CustomState extends javax.accessibility.AccessibleState {
        CustomState(String k) { super(k); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleState");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        boolean hasTD = false;
        for (Method m : c.getMethods()) if (m.getName().equals("toDisplayString")) hasTD = true;
        check(hasTD, "inherits toDisplayString");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleState active = javax.accessibility.AccessibleState.ACTIVE;
        check(active != null, "ACTIVE non-null");
        check(javax.accessibility.AccessibleState.ENABLED != null, "ENABLED non-null");
        check(javax.accessibility.AccessibleState.VISIBLE != null, "VISIBLE non-null");
        check(javax.accessibility.AccessibleState.SHOWING != null, "SHOWING non-null");
        check(active != javax.accessibility.AccessibleState.ENABLED, "distinct constants");

        check("active".equals(active.toDisplayString(Locale.US)), "ACTIVE localized");
        check("enabled".equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(Locale.US)),
                "ENABLED localized");
        check(active.toString() != null, "toString non-null");
        check(active.toString().equals(active.toDisplayString()), "toString == toDisplayString()");

        CustomState custom = new CustomState("customStateUnknownKey_xyz");
        check("customStateUnknownKey_xyz".equals(custom.toDisplayString(Locale.US)),
                "unknown state key falls back to raw key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
