/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedTable conformance: interface reflection.
 * @run main AccessibleExtendedTableTest
 */

public class AccessibleExtendedTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedTable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleTable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleRow"), "method getAccessibleRow");
        check(hasMethod(c, "getAccessibleColumn"), "method getAccessibleColumn");
        check(hasMethod(c, "getAccessibleIndex"), "method getAccessibleIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
