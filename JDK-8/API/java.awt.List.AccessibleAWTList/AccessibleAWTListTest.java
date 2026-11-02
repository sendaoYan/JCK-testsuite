/*
 * @test
 * @summary java.awt.List.AccessibleAWTList conformance: class reflection.
 * @run main AccessibleAWTListTest
 */

public class AccessibleAWTListTest {
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
        Class<?> c = Class.forName("java.awt.List$AccessibleAWTList");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.ActionListener", "java.awt.event.ItemListener", "javax.accessibility.AccessibleSelection"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "itemStateChanged"), "method itemStateChanged");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(hasMethod(c, "getAccessibleChild"), "method getAccessibleChild");
        check(hasMethod(c, "getAccessibleSelection"), "method getAccessibleSelection");
        check(hasMethod(c, "getAccessibleSelectionCount"), "method getAccessibleSelectionCount");
        check(hasMethod(c, "isAccessibleChildSelected"), "method isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "method addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "method removeAccessibleSelection");
        check(hasMethod(c, "clearAccessibleSelection"), "method clearAccessibleSelection");
        check(hasMethod(c, "selectAllAccessibleSelection"), "method selectAllAccessibleSelection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
