/*
 * @test
 * @summary java.rmi.activation.ActivationMonitor conformance: interface reflection.
 * @run main ActivationMonitorTest
 */

public class ActivationMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationMonitor");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "activeObject"), "declares activeObject");
        check(hasMethod(c, "inactiveGroup"), "declares inactiveGroup");
        check(hasMethod(c, "inactiveObject"), "declares inactiveObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
