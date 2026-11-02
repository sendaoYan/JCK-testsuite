/*
 * @test
 * @summary javax.management.remote.rmi._RMIConnection_Stub conformance: concrete class reflection.
 * @run main _RMIConnection_StubTest
 */

public class _RMIConnection_StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi._RMIConnection_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.rmi.CORBA.Stub".equals(c.getSuperclass().getName()), "superclass javax.rmi.CORBA.Stub");
        check(Class.forName("javax.management.remote.rmi.RMIConnection").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIConnection");
        check(hasMethod(c, "_ids"), "declares _ids");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
