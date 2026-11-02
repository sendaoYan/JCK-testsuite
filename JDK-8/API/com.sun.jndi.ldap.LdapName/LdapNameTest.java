/*
 * @test
 * @summary com.sun.jndi.ldap.LdapName conformance: hierarchy and declared members via reflection.
 * @run main LdapNameTest
 */

public class LdapNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapName", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapName"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.Name"), "implements javax.naming.Name");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("addAll"), "declares addAll");
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("compareTo"), "declares compareTo");
        check(ms.contains("endsWith"), "declares endsWith");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("escapeAttributeValue"), "declares escapeAttributeValue");
        check(ms.contains("get"), "declares get");
        check(ms.contains("getAll"), "declares getAll");
        check(ms.contains("getPrefix"), "declares getPrefix");
        check(ms.contains("getSuffix"), "declares getSuffix");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("isEmpty"), "declares isEmpty");
        check(ms.contains("isWhitespace"), "declares isWhitespace");
        check(ms.contains("matches"), "declares matches");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("setValuesCaseSensitive"), "declares setValuesCaseSensitive");
        check(ms.contains("size"), "declares size");
        check(ms.contains("startsWith"), "declares startsWith");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("unescapeAttributeValue"), "declares unescapeAttributeValue");
        check(ms.contains("writeObject"), "declares writeObject");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
