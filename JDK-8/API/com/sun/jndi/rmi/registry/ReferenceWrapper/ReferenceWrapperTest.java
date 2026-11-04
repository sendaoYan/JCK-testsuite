/*
 * @test
 * @summary com.sun.jndi.rmi.registry.ReferenceWrapper conformance: hierarchy and declared members via reflection.
 * @run main ReferenceWrapperTest
 */

public class ReferenceWrapperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.rmi.registry.ReferenceWrapper", false, null);
        check(c.getName().equals("com.sun.jndi.rmi.registry.ReferenceWrapper"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.UnicastRemoteObject"), "superclass java.rmi.server.UnicastRemoteObject");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jndi.rmi.registry.RemoteReference"), "implements com.sun.jndi.rmi.registry.RemoteReference");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getReference"), "declares getReference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
