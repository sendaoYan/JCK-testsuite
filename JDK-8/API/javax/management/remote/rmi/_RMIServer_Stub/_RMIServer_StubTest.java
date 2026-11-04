/*
 * @test
 * @summary javax.management.remote.rmi._RMIServer_Stub conformance: concrete class reflection.
 * @run main _RMIServer_StubTest
 */

public class _RMIServer_StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi._RMIServer_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.rmi.CORBA.Stub".equals(c.getSuperclass().getName()), "superclass javax.rmi.CORBA.Stub");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIServer");
        check(hasMethod(c, "_ids"), "declares _ids");
        check(hasMethod(c, "getVersion"), "declares getVersion");
        check(hasMethod(c, "newClient"), "declares newClient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
