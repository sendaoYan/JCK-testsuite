/*
 * @test
 * @summary com.sun.jmx.mbeanserver.MBeanInstantiator conformance: hierarchy and declared members via reflection.
 * @run main MBeanInstantiatorTest
 */

public class MBeanInstantiatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.MBeanInstantiator", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.MBeanInstantiator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkMBeanPermission"), "declares checkMBeanPermission");
        check(ms.contains("deserialize"), "declares deserialize");
        check(ms.contains("ensureClassAccess"), "declares ensureClassAccess");
        check(ms.contains("findClass"), "declares findClass");
        check(ms.contains("findClassWithDefaultLoaderRepository"), "declares findClassWithDefaultLoaderRepository");
        check(ms.contains("findConstructor"), "declares findConstructor");
        check(ms.contains("findSignatureClasses"), "declares findSignatureClasses");
        check(ms.contains("getClassLoader"), "declares getClassLoader");
        check(ms.contains("getClassLoaderRepository"), "declares getClassLoaderRepository");
        check(ms.contains("instantiate"), "declares instantiate");
        check(ms.contains("loadClass"), "declares loadClass");
        check(ms.contains("loadSignatureClasses"), "declares loadSignatureClasses");
        check(ms.contains("testCreation"), "declares testCreation");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
