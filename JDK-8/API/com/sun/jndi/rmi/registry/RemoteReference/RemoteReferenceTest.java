/*
 * @test
 * @summary com.sun.jndi.rmi.registry.RemoteReference conformance: hierarchy and declared members via reflection.
 * @run main RemoteReferenceTest
 */

public class RemoteReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.rmi.registry.RemoteReference", false, null);
        check(c.getName().equals("com.sun.jndi.rmi.registry.RemoteReference"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.rmi.Remote"), "implements java.rmi.Remote");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getReference"), "declares getReference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
