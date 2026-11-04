/*
 * @test
 * @summary java.rmi.server.RMISocketFactory conformance: abstract class reflection.
 * @run main RMISocketFactoryTest
 */

public class RMISocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMISocketFactory");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Class.forName("java.rmi.server.RMIClientSocketFactory").isAssignableFrom(c), "implements/extends java.rmi.server.RMIClientSocketFactory");
        check(Class.forName("java.rmi.server.RMIServerSocketFactory").isAssignableFrom(c), "implements/extends java.rmi.server.RMIServerSocketFactory");
        check(hasMethod(c, "createServerSocket"), "declares createServerSocket");
        check(hasMethod(c, "createSocket"), "declares createSocket");
        check(hasMethod(c, "getDefaultSocketFactory"), "declares getDefaultSocketFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
