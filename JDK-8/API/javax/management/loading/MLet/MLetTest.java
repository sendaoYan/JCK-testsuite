/*
 * @test
 * @summary javax.management.loading.MLet conformance: concrete class reflection.
 * @run main MLetTest
 */

public class MLetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLet");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.net.URLClassLoader".equals(c.getSuperclass().getName()), "superclass java.net.URLClassLoader");
        check(Class.forName("javax.management.loading.MLetMBean").isAssignableFrom(c), "implements/extends javax.management.loading.MLetMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("java.io.Externalizable").isAssignableFrom(c), "implements/extends java.io.Externalizable");
        check(hasMethod(c, "addURL"), "declares addURL");
        check(hasMethod(c, "getLibraryDirectory"), "declares getLibraryDirectory");
        check(hasMethod(c, "getMBeansFromURL"), "declares getMBeansFromURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
