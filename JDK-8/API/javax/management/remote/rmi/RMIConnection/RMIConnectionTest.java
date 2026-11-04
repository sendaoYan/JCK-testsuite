/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnection conformance: interface reflection.
 * @run main RMIConnectionTest
 */

public class RMIConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnection");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        check(hasMethod(c, "close"), "declares close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
