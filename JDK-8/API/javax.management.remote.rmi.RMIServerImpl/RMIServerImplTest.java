/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl conformance: abstract class reflection.
 * @run main RMIServerImplTest
 */

public class RMIServerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIServer");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "getDefaultClassLoader"), "declares getDefaultClassLoader");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
