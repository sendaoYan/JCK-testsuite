/*
 * @test
 * @summary com.sun.jndi.rmi.registry.ReferenceWrapper_Stub conformance: hierarchy and declared members via reflection.
 * @run main ReferenceWrapper_StubTest
 */

public class ReferenceWrapper_StubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.rmi.registry.ReferenceWrapper_Stub", false, null);
        check(c.getName().equals("com.sun.jndi.rmi.registry.ReferenceWrapper_Stub"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RemoteStub"), "superclass java.rmi.server.RemoteStub");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jndi.rmi.registry.RemoteReference"), "implements com.sun.jndi.rmi.registry.RemoteReference");
        check(in.contains("java.rmi.Remote"), "implements java.rmi.Remote");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getReference"), "declares getReference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
