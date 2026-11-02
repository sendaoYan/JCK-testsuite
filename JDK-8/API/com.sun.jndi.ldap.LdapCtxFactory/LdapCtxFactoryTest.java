/*
 * @test
 * @summary com.sun.jndi.ldap.LdapCtxFactory conformance: hierarchy and declared members via reflection.
 * @run main LdapCtxFactoryTest
 */

public class LdapCtxFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapCtxFactory", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapCtxFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.spi.ObjectFactory"), "implements javax.naming.spi.ObjectFactory");
        check(in.contains("javax.naming.spi.InitialContextFactory"), "implements javax.naming.spi.InitialContextFactory");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createTypeNameAttr"), "declares createTypeNameAttr");
        check(ms.contains("getClassesAux"), "declares getClassesAux");
        check(ms.contains("getInitialContext"), "declares getInitialContext");
        check(ms.contains("getLdapCtxFromUrl"), "declares getLdapCtxFromUrl");
        check(ms.contains("getLdapCtxInstance"), "declares getLdapCtxInstance");
        check(ms.contains("getObjectInstance"), "declares getObjectInstance");
        check(ms.contains("getTypeNames"), "declares getTypeNames");
        check(ms.contains("getURLs"), "declares getURLs");
        check(ms.contains("getUsingURL"), "declares getUsingURL");
        check(ms.contains("getUsingURLs"), "declares getUsingURLs");
        check(ms.contains("isLdapRef"), "declares isLdapRef");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
