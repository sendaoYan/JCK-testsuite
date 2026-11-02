/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectorServer conformance: concrete class reflection.
 * @run main RMIConnectorServerTest
 */

public class RMIConnectorServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectorServer");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.remote.JMXConnectorServer".equals(c.getSuperclass().getName()), "superclass javax.management.remote.JMXConnectorServer");
        check(hasMethod(c, "getAddress"), "declares getAddress");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "isActive"), "declares isActive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
