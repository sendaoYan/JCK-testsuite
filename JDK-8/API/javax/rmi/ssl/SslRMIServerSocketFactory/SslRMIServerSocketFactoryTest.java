/*
 * @test
 * @summary javax.rmi.ssl.SslRMIServerSocketFactory conformance: concrete class reflection.
 * @run main SslRMIServerSocketFactoryTest
 */

public class SslRMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.ssl.SslRMIServerSocketFactory");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.rmi.server.RMIServerSocketFactory").isAssignableFrom(c), "implements/extends java.rmi.server.RMIServerSocketFactory");
        check(hasMethod(c, "createServerSocket"), "declares createServerSocket");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getEnabledCipherSuites"), "declares getEnabledCipherSuites");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
