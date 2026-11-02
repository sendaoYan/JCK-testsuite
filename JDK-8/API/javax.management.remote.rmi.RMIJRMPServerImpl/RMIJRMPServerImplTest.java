/*
 * @test
 * @summary javax.management.remote.rmi.RMIJRMPServerImpl conformance: concrete class reflection.
 * @run main RMIJRMPServerImplTest
 */

public class RMIJRMPServerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.remote.rmi.RMIServerImpl".equals(c.getSuperclass().getName()), "superclass javax.management.remote.rmi.RMIServerImpl");
        check(hasMethod(c, "toStub"), "declares toStub");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
