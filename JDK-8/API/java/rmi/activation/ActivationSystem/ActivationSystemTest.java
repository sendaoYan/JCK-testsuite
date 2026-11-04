/*
 * @test
 * @summary java.rmi.activation.ActivationSystem conformance: interface reflection.
 * @run main ActivationSystemTest
 */

public class ActivationSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationSystem");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "activeGroup"), "declares activeGroup");
        check(hasMethod(c, "getActivationDesc"), "declares getActivationDesc");
        check(hasMethod(c, "getActivationGroupDesc"), "declares getActivationGroupDesc");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
