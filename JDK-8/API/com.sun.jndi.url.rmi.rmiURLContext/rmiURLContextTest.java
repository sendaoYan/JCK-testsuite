/*
 * @test
 * @summary com.sun.jndi.url.rmi.rmiURLContext conformance: hierarchy and declared members via reflection.
 * @run main rmiURLContextTest
 */

public class rmiURLContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.url.rmi.rmiURLContext", false, null);
        check(c.getName().equals("com.sun.jndi.url.rmi.rmiURLContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.url.GenericURLContext"), "superclass com.sun.jndi.toolkit.url.GenericURLContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getRootURLContext"), "declares getRootURLContext");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
