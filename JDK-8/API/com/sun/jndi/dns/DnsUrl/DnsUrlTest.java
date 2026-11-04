/*
 * @test
 * @summary com.sun.jndi.dns.DnsUrl conformance: hierarchy and declared members via reflection.
 * @run main DnsUrlTest
 */

public class DnsUrlTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.DnsUrl", false, null);
        check(c.getName().equals("com.sun.jndi.dns.DnsUrl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.url.Uri"), "superclass com.sun.jndi.toolkit.url.Uri");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkSchemeOnly"), "declares checkSchemeOnly");
        check(ms.contains("fromList"), "declares fromList");
        check(ms.contains("getDomain"), "declares getDomain");
        check(ms.contains("isDnsSchemeOnly"), "declares isDnsSchemeOnly");
        check(ms.contains("isSchemeOnly"), "declares isSchemeOnly");
        check(ms.contains("newInvalidURISchemeException"), "declares newInvalidURISchemeException");
        check(ms.contains("parseMode"), "declares parseMode");
        check(ms.contains("validateURI"), "declares validateURI");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
