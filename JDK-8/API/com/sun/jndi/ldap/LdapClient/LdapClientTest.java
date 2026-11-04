/*
 * @test
 * @summary com.sun.jndi.ldap.LdapClient conformance: hierarchy and declared members via reflection.
 * @run main LdapClientTest
 */

public class LdapClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapClient", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapClient"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jndi.ldap.pool.PooledConnection"), "implements com.sun.jndi.ldap.pool.PooledConnection");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("addUnsolicited"), "declares addUnsolicited");
        check(ms.contains("authenticate"), "declares authenticate");
        check(ms.contains("authenticateCalled"), "declares authenticateCalled");
        check(ms.contains("clearSearchReply"), "declares clearSearchReply");
        check(ms.contains("close"), "declares close");
        check(ms.contains("closeConnection"), "declares closeConnection");
        check(ms.contains("compare"), "declares compare");
        check(ms.contains("delete"), "declares delete");
        check(ms.contains("encodeAttribute"), "declares encodeAttribute");
        check(ms.contains("encodeControls"), "declares encodeControls");
        check(ms.contains("encodePassword"), "declares encodePassword");
        check(ms.contains("ensureOpen"), "declares ensureOpen");
        check(ms.contains("extendedOp"), "declares extendedOp");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("forceClose"), "declares forceClose");
        check(ms.contains("getErrorMessage"), "declares getErrorMessage");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getSearchReply"), "declares getSearchReply");
        check(ms.contains("hasNoValue"), "declares hasNoValue");
        check(ms.contains("incRefCount"), "declares incRefCount");
        check(ms.contains("isBinaryValued"), "declares isBinaryValued");
        check(ms.contains("isUpgradedToStartTls"), "declares isUpgradedToStartTls");
        check(ms.contains("ldapBind"), "declares ldapBind");
        check(ms.contains("moddn"), "declares moddn");
        check(ms.contains("modify"), "declares modify");
        check(ms.contains("notifyUnsolicited"), "declares notifyUnsolicited");
        check(ms.contains("parseAttribute"), "declares parseAttribute");
        check(ms.contains("parseAttributeValue"), "declares parseAttributeValue");
        check(ms.contains("parseControls"), "declares parseControls");
        check(ms.contains("parseExtResponse"), "declares parseExtResponse");
        check(ms.contains("parseResult"), "declares parseResult");
        check(ms.contains("processConnectionClosure"), "declares processConnectionClosure");
        check(ms.contains("processReply"), "declares processReply");
        check(ms.contains("processUnsolicited"), "declares processUnsolicited");
        check(ms.contains("removeUnsolicited"), "declares removeUnsolicited");
        check(ms.contains("search"), "declares search");
        check(ms.contains("usingSaslStreams"), "declares usingSaslStreams");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
