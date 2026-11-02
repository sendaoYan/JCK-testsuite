/*
 * @test
 * @summary com.sun.jndi.ldap.pool.Pool conformance: hierarchy and declared members via reflection.
 * @run main PoolTest
 */

public class PoolTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.pool.Pool", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.pool.Pool"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("d"), "declares d");
        check(ms.contains("expire"), "declares expire");
        check(ms.contains("expungeStaleConnections"), "declares expungeStaleConnections");
        check(ms.contains("getConnections"), "declares getConnections");
        check(ms.contains("getPooledConnection"), "declares getPooledConnection");
        check(ms.contains("showStats"), "declares showStats");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
