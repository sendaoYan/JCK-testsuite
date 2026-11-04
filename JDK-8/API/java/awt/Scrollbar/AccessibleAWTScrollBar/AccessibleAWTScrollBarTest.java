/*
 * @test
 * @summary java.awt.Scrollbar.AccessibleAWTScrollBar conformance: class reflection.
 * @run main AccessibleAWTScrollBarTest
 */

public class AccessibleAWTScrollBarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Scrollbar$AccessibleAWTScrollBar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleValue"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleValue"), "method getAccessibleValue");
        check(hasMethod(c, "getCurrentAccessibleValue"), "method getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "method setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "method getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "method getMaximumAccessibleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
