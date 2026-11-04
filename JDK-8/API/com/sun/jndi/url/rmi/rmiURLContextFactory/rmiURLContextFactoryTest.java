/*
 * @test
 * @summary com.sun.jndi.url.rmi.rmiURLContextFactory conformance: hierarchy and declared members via reflection.
 * @run main rmiURLContextFactoryTest
 */

public class rmiURLContextFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.url.rmi.rmiURLContextFactory", false, null);
        check(c.getName().equals("com.sun.jndi.url.rmi.rmiURLContextFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.spi.ObjectFactory"), "implements javax.naming.spi.ObjectFactory");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getObjectInstance"), "declares getObjectInstance");
        check(ms.contains("getUsingURL"), "declares getUsingURL");
        check(ms.contains("getUsingURLs"), "declares getUsingURLs");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
