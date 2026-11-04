/*
 * @test
 * @summary java.awt.ContainerOrderFocusTraversalPolicy conformance: class reflection.
 * @run main ContainerOrderFocusTraversalPolicyTest
 */

public class ContainerOrderFocusTraversalPolicyTest {
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
        Class<?> c = Class.forName("java.awt.ContainerOrderFocusTraversalPolicy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.FocusTraversalPolicy"), "superclass java.awt.FocusTraversalPolicy");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getComponentAfter"), "method getComponentAfter");
        check(hasMethod(c, "getComponentBefore"), "method getComponentBefore");
        check(hasMethod(c, "getFirstComponent"), "method getFirstComponent");
        check(hasMethod(c, "getLastComponent"), "method getLastComponent");
        check(hasMethod(c, "getDefaultComponent"), "method getDefaultComponent");
        check(hasMethod(c, "setImplicitDownCycleTraversal"), "method setImplicitDownCycleTraversal");
        check(hasMethod(c, "getImplicitDownCycleTraversal"), "method getImplicitDownCycleTraversal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
