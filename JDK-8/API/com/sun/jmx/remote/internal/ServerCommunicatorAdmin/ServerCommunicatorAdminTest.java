/*
 * @test
 * @summary com.sun.jmx.remote.internal.ServerCommunicatorAdmin conformance: hierarchy and declared members via reflection.
 * @run main ServerCommunicatorAdminTest
 */

public class ServerCommunicatorAdminTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ServerCommunicatorAdmin", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ServerCommunicatorAdmin"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("doStop"), "declares doStop");
        check(ms.contains("logtime"), "declares logtime");
        check(ms.contains("reqIncoming"), "declares reqIncoming");
        check(ms.contains("rspOutgoing"), "declares rspOutgoing");
        check(ms.contains("terminate"), "declares terminate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
