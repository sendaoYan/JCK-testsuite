/*
 * @test
 * @summary javax.naming.ldap.LdapName conformance: white-box structure + black-box behavior.
 * @run main LdapNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;

public class LdapNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapName");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Name.class.isAssignableFrom(c), "implements Name");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == List.class), "ctor(List)");
        for (String m : new String[]{"size", "isEmpty", "getAll", "get", "getRdn", "getPrefix", "getSuffix", "startsWith", "endsWith", "add", "addAll", "remove", "getRdns", "clone", "toString", "equals", "compareTo", "hashCode"}) {
            check(hasMethod(c, m), m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        LdapName ln = new LdapName("cn=John,dc=example,dc=com");
        check(ln.size() == 3, "size 3");
        check(!ln.isEmpty(), "not empty");
        // LdapName stores least-significant RDN first (index 0 == rightmost).
        check("dc=com".equals(ln.get(0)), "get(0) rightmost");
        check("dc=example".equals(ln.get(1)), "get(1)");
        check("cn=John".equals(ln.get(2)), "get(2) leftmost");
        check("cn=John,dc=example,dc=com".equals(ln.toString()), "toString");
        check("dc=com".equals(ln.getRdn(0).toString()), "getRdn(0)");
        check("cn=John".equals(ln.getRdn(2).toString()), "getRdn(2)");

        List<String> all = Collections.list(ln.getAll());
        check(all.equals(Arrays.asList("dc=com", "dc=example", "cn=John")), "getAll order");

        List<Rdn> rdns = ln.getRdns();
        check(rdns.size() == 3, "getRdns size");
        check("dc=com".equals(rdns.get(0).toString()), "getRdns 0");

        // prefix/suffix (least-significant prefix)
        check("dc=com".equals(ln.getPrefix(1).toString()), "getPrefix(1)");
        check("cn=John,dc=example".equals(ln.getSuffix(1).toString()), "getSuffix(1)");

        // startsWith / endsWith
        check(ln.startsWith(new LdapName("dc=com")), "startsWith dc=com");
        check(ln.startsWith(new LdapName("dc=example,dc=com")), "startsWith dc=example,dc=com");
        check(!ln.startsWith(new LdapName("cn=John")), "not startsWith cn=John");
        check(ln.endsWith(new LdapName("cn=John")), "endsWith cn=John");
        check(!ln.endsWith(new LdapName("dc=com")), "not endsWith dc=com");

        // equals / hashCode / compareTo / clone
        LdapName ln2 = new LdapName("cn=John,dc=example,dc=com");
        check(ln.equals(ln2), "equals");
        check(ln.hashCode() == ln2.hashCode(), "hashCode");
        check(ln.compareTo(ln2) == 0, "compareTo equal 0");
        LdapName cl = (LdapName) ln.clone();
        check(cl != ln, "clone distinct");
        check(cl.equals(ln), "clone equals");
        check(new LdapName("cn=a").compareTo(new LdapName("cn=b")) < 0, "compareTo ordering");

        // add / addAll / remove
        LdapName m = (LdapName) ln.clone();
        m.add("ou=People");
        check("ou=People,cn=John,dc=example,dc=com".equals(m.toString()), "add(String) prepends");
        m.add(new Rdn("o=Corp"));
        check("o=Corp,ou=People,cn=John,dc=example,dc=com".equals(m.toString()), "add(Rdn) prepends");
        m.add(1, "c=US");
        check("o=Corp,ou=People,cn=John,dc=example,c=US,dc=com".equals(m.toString()), "add(int,String)");

        LdapName m2 = new LdapName("cn=John");
        m2.addAll(new LdapName("dc=example,dc=com"));
        check("dc=example,dc=com,cn=John".equals(m2.toString()), "addAll(Name) appends most-significant end");

        LdapName m3 = new LdapName("cn=John");
        m3.addAll(0, new LdapName("dc=example,dc=com"));
        check("cn=John,dc=example,dc=com".equals(m3.toString()), "addAll(0,Name) at least-significant end");

        Object removed = m.remove(1);
        check("c=US".equals(String.valueOf(removed)), "remove(int) returns component");
        check("o=Corp,ou=People,cn=John,dc=example,dc=com".equals(m.toString()), "after remove");

        // empty name
        LdapName empty = new LdapName("");
        check(empty.isEmpty(), "empty isEmpty");
        check(empty.size() == 0, "empty size");
        check("".equals(empty.toString()), "empty toString");

        // invalid name
        try {
            new LdapName("=John");
            check(false, "invalid should throw");
        } catch (InvalidNameException expected) {
            check(true, "invalid throws InvalidNameException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
