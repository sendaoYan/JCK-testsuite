/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl conformance: concrete class reflection.
 * @run main RMIConnectionImplTest
 */

public class RMIConnectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.remote.rmi.RMIConnection").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIConnection");
        check(Class.forName("java.rmi.server.Unreferenced").isAssignableFrom(c), "implements/extends java.rmi.server.Unreferenced");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        check(hasMethod(c, "close"), "declares close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
