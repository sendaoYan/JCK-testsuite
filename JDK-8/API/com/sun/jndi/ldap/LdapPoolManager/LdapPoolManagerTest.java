/*
 * @test
 * @summary com.sun.jndi.ldap.LdapPoolManager conformance: hierarchy and declared members via reflection.
 * @run main LdapPoolManagerTest
 */

public class LdapPoolManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapPoolManager", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapPoolManager"), "binary name");
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
        check(ms.contains("findPool"), "declares findPool");
        check(ms.contains("getInteger"), "declares getInteger");
        check(ms.contains("getLdapClient"), "declares getLdapClient");
        check(ms.contains("getLong"), "declares getLong");
        check(ms.contains("getProperty"), "declares getProperty");
        check(ms.contains("isPoolingAllowed"), "declares isPoolingAllowed");
        check(ms.contains("showStats"), "declares showStats");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
