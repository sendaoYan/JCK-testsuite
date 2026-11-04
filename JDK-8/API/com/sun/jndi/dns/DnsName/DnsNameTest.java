/*
 * @test
 * @summary com.sun.jndi.dns.DnsName conformance: hierarchy and declared members via reflection.
 * @run main DnsNameTest
 */

public class DnsNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.DnsName", false, null);
        check(c.getName().equals("com.sun.jndi.dns.DnsName"), "binary name");
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
        check(ms.contains("compareLabels"), "declares compareLabels");
        check(ms.contains("compareRange"), "declares compareRange");
        check(ms.contains("compareTo"), "declares compareTo");
        check(ms.contains("endsWith"), "declares endsWith");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("escape"), "declares escape");
        check(ms.contains("get"), "declares get");
        check(ms.contains("getAll"), "declares getAll");
        check(ms.contains("getEscapedOctet"), "declares getEscapedOctet");
        check(ms.contains("getKey"), "declares getKey");
        check(ms.contains("getOctets"), "declares getOctets");
        check(ms.contains("getPrefix"), "declares getPrefix");
        check(ms.contains("getSuffix"), "declares getSuffix");
        check(ms.contains("hasRootLabel"), "declares hasRootLabel");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("isDigit"), "declares isDigit");
        check(ms.contains("isEmpty"), "declares isEmpty");
        check(ms.contains("isHostName"), "declares isHostName");
        check(ms.contains("isHostNameChar"), "declares isHostNameChar");
        check(ms.contains("isHostNameLabel"), "declares isHostNameLabel");
        check(ms.contains("keyForLabel"), "declares keyForLabel");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("size"), "declares size");
        check(ms.contains("startsWith"), "declares startsWith");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("verifyLabel"), "declares verifyLabel");
        check(ms.contains("writeObject"), "declares writeObject");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
