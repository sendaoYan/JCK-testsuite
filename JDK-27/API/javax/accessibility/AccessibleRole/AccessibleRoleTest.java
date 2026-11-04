/*
 * @test
 * @summary javax.accessibility.AccessibleRole conformance: white-box structure + black-box behavior.
 * @run main AccessibleRoleTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleRoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class CustomRole extends javax.accessibility.AccessibleRole {
        CustomRole(String k) { super(k); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleRole");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        boolean hasTD = false;
        for (Method m : c.getMethods()) if (m.getName().equals("toDisplayString")) hasTD = true;
        check(hasTD, "inherits toDisplayString");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleRole alert = javax.accessibility.AccessibleRole.ALERT;
        check(alert != null, "ALERT non-null");
        check(javax.accessibility.AccessibleRole.PUSH_BUTTON != null, "PUSH_BUTTON non-null");
        check(javax.accessibility.AccessibleRole.TABLE != null, "TABLE non-null");
        check(javax.accessibility.AccessibleRole.UNKNOWN != null, "UNKNOWN non-null");
        check(alert != javax.accessibility.AccessibleRole.PUSH_BUTTON, "distinct constants");

        // Known keys are localized via the resource bundle (en_US).
        check("push button".equals(javax.accessibility.AccessibleRole.PUSH_BUTTON.toDisplayString(Locale.US)),
                "PUSH_BUTTON localized");
        check("alert".equals(alert.toDisplayString(Locale.US)), "ALERT localized");
        check(alert.toString() != null, "toString non-null");
        check(alert.toString().equals(alert.toDisplayString()), "toString == toDisplayString()");

        // Unknown keys fall back to the raw key.
        CustomRole custom = new CustomRole("customRoleUnknownKey_xyz");
        check("customRoleUnknownKey_xyz".equals(custom.toDisplayString(Locale.US)),
                "unknown role key falls back to raw key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
