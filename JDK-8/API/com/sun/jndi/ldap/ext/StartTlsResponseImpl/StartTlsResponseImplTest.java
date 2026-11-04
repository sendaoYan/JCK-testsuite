/*
 * @test
 * @summary com.sun.jndi.ldap.ext.StartTlsResponseImpl conformance: hierarchy and declared members via reflection.
 * @run main StartTlsResponseImplTest
 */

public class StartTlsResponseImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.ext.StartTlsResponseImpl", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.ext.StartTlsResponseImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.naming.ldap.StartTlsResponse"), "superclass javax.naming.ldap.StartTlsResponse");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("close"), "declares close");
        check(ms.contains("getDefaultFactory"), "declares getDefaultFactory");
        check(ms.contains("getPeerPrincipal"), "declares getPeerPrincipal");
        check(ms.contains("negotiate"), "declares negotiate");
        check(ms.contains("setConnection"), "declares setConnection");
        check(ms.contains("setEnabledCipherSuites"), "declares setEnabledCipherSuites");
        check(ms.contains("setHostnameVerifier"), "declares setHostnameVerifier");
        check(ms.contains("startHandshake"), "declares startHandshake");
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
