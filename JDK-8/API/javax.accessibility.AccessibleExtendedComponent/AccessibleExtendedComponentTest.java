/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedComponent conformance: interface reflection.
 * @run main AccessibleExtendedComponentTest
 */

public class AccessibleExtendedComponentTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedComponent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleComponent"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getToolTipText"), "method getToolTipText");
        check(hasMethod(c, "getTitledBorderText"), "method getTitledBorderText");
        check(hasMethod(c, "getAccessibleKeyBinding"), "method getAccessibleKeyBinding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
