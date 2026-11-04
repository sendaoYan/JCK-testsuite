/*
 * @test
 * @summary com.sun.jndi.ldap.LdapURL conformance: hierarchy and declared members via reflection.
 * @run main LdapURLTest
 */

public class LdapURLTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapURL", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapURL"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.url.Uri"), "superclass com.sun.jndi.toolkit.url.Uri");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("fromList"), "declares fromList");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getDN"), "declares getDN");
        check(ms.contains("getExtensions"), "declares getExtensions");
        check(ms.contains("getFilter"), "declares getFilter");
        check(ms.contains("getScope"), "declares getScope");
        check(ms.contains("hasQueryComponents"), "declares hasQueryComponents");
        check(ms.contains("isLdapSchemeOnly"), "declares isLdapSchemeOnly");
        check(ms.contains("isSchemeOnly"), "declares isSchemeOnly");
        check(ms.contains("newInvalidURISchemeException"), "declares newInvalidURISchemeException");
        check(ms.contains("parseMode"), "declares parseMode");
        check(ms.contains("parsePathAndQuery"), "declares parsePathAndQuery");
        check(ms.contains("toUrlString"), "declares toUrlString");
        check(ms.contains("useSsl"), "declares useSsl");
        check(ms.contains("validateURI"), "declares validateURI");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
