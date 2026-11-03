/*
 * @test
 * @summary javax.accessibility.Accessible conformance: white-box structure + black-box behavior.
 * @run main AccessibleTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // Minimal AccessibleContext for exercising the Accessible contract.
    static class MinContext extends javax.accessibility.AccessibleContext {
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public Locale getLocale() { return Locale.ROOT; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.Accessible");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleContext"), "declares getAccessibleContext");
    }

    static void testBlackBox() throws Exception {
        final javax.accessibility.AccessibleContext ctx = new MinContext();
        javax.accessibility.Accessible a = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return ctx; }
        };
        check(a.getAccessibleContext() == ctx, "getAccessibleContext returns same instance");
        ctx.setAccessibleName("name");
        check("name".equals(a.getAccessibleContext().getAccessibleName()), "context state reachable through Accessible");

        // A subclass that is not accessible may return null.
        javax.accessibility.Accessible empty = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
        check(empty.getAccessibleContext() == null, "null context allowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
