/*
 * @test
 * @summary java.awt.DefaultFocusTraversalPolicy conformance: class reflection.
 * @run main DefaultFocusTraversalPolicyTest
 */

public class DefaultFocusTraversalPolicyTest {
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
        Class<?> c = Class.forName("java.awt.DefaultFocusTraversalPolicy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.ContainerOrderFocusTraversalPolicy"), "superclass java.awt.ContainerOrderFocusTraversalPolicy");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
