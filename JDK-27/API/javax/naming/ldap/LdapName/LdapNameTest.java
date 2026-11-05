/*
 * @test
 * @summary javax.naming.ldap.LdapName conformance: white-box structure + black-box behavior.
 * @run main LdapNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dLnParseRoundTrip();
        dLnIndexSemantics();
        dLnEqualityAndCompare();
        dLnMutation();
        dLnPrefixSuffix();
        dLnNegative();
        dLnSerialization();
        System.out.println("deep: LdapName contracts OK");
    }

    static void dLnParseRoundTrip() throws Exception {
        String[] cases = {
            "cn=duke", "CN=duke", "cn=Duke, ou=Java, O=Sun",
            "cn=duke+sn=elliot", "cn=#04024869", "cn=\\#hash",
            "cn=duke\\, jr", "cn=\\\"quoted\\\"", "cn=lead\\ space",
            "cn=trail\\ ", "cn=a\\\\b", "cn=\\4F\\4B", "ou=\\e9\\e8",
            "cn=lu\\c4\\8di\\c4\\87",
        };
        for (String s : cases) {
            LdapName n = new LdapName(s);
            check(new LdapName(n.toString()).equals(n), "round-trip: " + s);
            check(n.startsWith(n) && n.endsWith(n), "self prefix/suffix: " + s);
        }
        // hex pair escapes decode to characters
        LdapName ok = new LdapName("cn=\\4F\\4B");
        check("OK".equals(ok.get(ok.size() - 1).substring(3)), "hex pair decodes");
        // UTF-8 hex escapes decode
        LdapName uni = new LdapName("cn=lu\\c4\\8di\\c4\\87");
        check(uni.toString().startsWith("cn=lu"), "UTF-8 hex escape prefix");
        // trailing space escape preserved
        check(new LdapName("cn=trail\\ ").toString().equals("cn=trail\\ "),
                "trailing space stays escaped");
        // string form is canonical for the parsed value
        check("cn=Duke, ou=Java, O=Sun".equals(new LdapName("cn=Duke, ou=Java, O=Sun").toString()),
                "spacing normalized to no extra spaces");
    }

    static void dLnIndexSemantics() throws Exception {
        LdapName n = new LdapName("cn=Duke, ou=Java, O=Sun");
        check(n.size() == 3, "three RDNs");
        // index 0 is the RIGHTMOST RDN of the string form
        check("O=Sun".equals(n.get(0)), "get(0) is rightmost");
        check("cn=Duke".equals(n.get(n.size() - 1)), "get(size-1) is leftmost");
        java.util.List<String> all = new java.util.ArrayList<>();
        for (java.util.Enumeration<String> e = n.getAll(); e.hasMoreElements(); )
            all.add(e.nextElement());
        check(all.get(0).equals("O=Sun") && all.get(2).equals("cn=Duke"),
                "getAll order follows index order");
        java.util.List<Rdn> rdns = n.getRdns();
        check(rdns.size() == 3, "rdns list size");
        check(rdns.get(0).toString().equals("O=Sun"), "rdns list index 0 rightmost");
        check(rdns.get(2).toString().equals("cn=Duke"), "rdns list last leftmost");
        check(n.getPrefix(1).toString().equals("O=Sun"), "prefix(1) from rightmost");
        check(n.getSuffix(2).toString().equals("cn=Duke"), "suffix(2) keeps leftmost");
    }

    static void dLnEqualityAndCompare() throws Exception {
        check(new LdapName("CN=duke").equals(new LdapName("cn=duke")), "type case-insensitive");
        check(new LdapName("cn=Duke").equals(new LdapName("cn=duke")), "value case-insensitive");
        check(new LdapName("CN=duke").hashCode() == new LdapName("cn=duke").hashCode(),
                "hashCode agrees");
        check(!new LdapName("cn=duke").equals(new LdapName("sn=duke")), "different types unequal");
        check(!new LdapName("cn=duke").equals("cn=duke"), "non-LdapName never equal");
        check(!new LdapName("cn=duke").equals(null), "null never equal");
        // multivalue RDN order irrelevant
        check(new LdapName("cn=a+sn=b").equals(new LdapName("sn=b+cn=a")),
                "multi-value RDN order-insensitive");
        check(new LdapName("cn=a, dc=x").equals(new LdapName("cn=a,dc=x")), "whitespace irrelevant");
        // comparison
        check(new LdapName("cn=a").compareTo(new LdapName("cn=b")) < 0, "compareTo order");
        check(new LdapName("cn=a").compareTo(new LdapName("cn=a")) == 0, "compareTo equal");
        check(new LdapName("cn=a").compareTo(new LdapName("cn=a,ou=x")) != 0,
                "shorter vs longer names differ");
        checkThrowsCCE();
        // byte-array values compare element-wise
        check(new LdapName("cn=#04024869").equals(new LdapName("cn=#04024869")),
                "hex values equal");
    }

    static void checkThrowsCCE() throws Exception {
        boolean cce = false;
        try { new LdapName("cn=a").compareTo(new Object()); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "compareTo non-LdapName -> CCE");
    }

    static void dLnMutation() throws Exception {
        LdapName n = new LdapName("cn=x");
        n.add("ou=y");
        check(n.size() == 2 && n.toString().equals("ou=y,cn=x"), "add appends as leftmost RDN");
        n.add(0, "o=z");
        check(n.toString().equals("ou=y,cn=x,o=z"), "add(0,..) inserts at rightmost");
        Object removed = n.remove(n.size() - 1);
        check("ou=y".equals(removed), "remove returns removed RDN");
        check(n.toString().equals("cn=x,o=z"), "remove drops leftmost when index size-1");
        n.addAll(new LdapName("dc=example,dc=com"));
        check(n.size() == 4, "addAll(LdapName) accepted");
        boolean ine = false;
        try { n.addAll(new CompositeName("a/b")); }
        catch (InvalidNameException e) { ine = true; }
        check(ine, "addAll(CompositeName) -> InvalidNameException");
        checkThrowsAIOOBE(n);
        // clone independence
        LdapName clone = (LdapName) n.clone();
        check(clone.equals(n), "clone equals");
        clone.add("extra=r");
        check(n.size() == 4, "clone independent");
    }

    static void checkThrowsAIOOBE(LdapName n) throws Exception {
        boolean a1 = false, a2 = false;
        try { n.get(n.size()); } catch (IndexOutOfBoundsException e) { a1 = true; }
        try { n.remove(-1); } catch (IndexOutOfBoundsException e) { a2 = true; }
        check(a1, "get(size) -> IndexOutOfBoundsException");
        check(a2, "remove(-1) -> IndexOutOfBoundsException");
    }

    static void dLnPrefixSuffix() throws Exception {
        LdapName n = new LdapName("cn=Duke, ou=Java, O=Sun");
        check(!n.startsWith(new LdapName("cn=Duke")), "startsWith uses index-0 (rightmost) order");
        check(n.endsWith(new LdapName("cn=Duke")), "endsWith matches leftmost RDN chain");
        check(n.startsWith(new LdapName("O=Sun")), "startsWith matches rightmost RDN chain");
        check(n.startsWith(new LdapName("")), "empty name is a prefix");
        check(n.endsWith(new LdapName("")), "empty name is a suffix");
        check(!n.startsWith(new CompositeName("x")), "startsWith(non-LdapName) false");
        check(!n.endsWith(new CompositeName("x")), "endsWith(non-LdapName) false");
    }

    static void dLnNegative() {
        String[] bad = {"noequals", "=duke", "cn", ",", ",cn=duke", "\\4", "cn=a\\", "cn=a,,ou=b"};
        for (String s : bad) {
            boolean ine = false;
            try { new LdapName(s); }
            catch (InvalidNameException e) {
                ine = true;
                check(e.getMessage() != null && e.getMessage().contains(s),
                        "InvalidNameException carries input: " + s);
            }
            check(ine, "invalid: " + s);
        }
        // tolerated forms
        try {
            check(new LdapName("cn=").size() == 1, "empty value tolerated");
            check(new LdapName("cn=duke,").size() == 2, "trailing comma yields empty RDN");
            check(new LdapName("cn= ").size() == 1, "space value tolerated");
            check(new LdapName("cn=a\\ ").size() == 1, "escaped trailing space tolerated");
        } catch (InvalidNameException e) {
            throw new AssertionError("tolerated form rejected: " + e.getMessage());
        }
    }

    static void dLnSerialization() throws Exception {
        LdapName n = new LdapName("cn=Duke, ou=Java, O=Sun");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            LdapName back = (LdapName) ois.readObject();
            check(back.equals(n), "round-trip equal");
            check(back.size() == 3, "round-trip size");
            check(back.get(0).equals("O=Sun"), "round-trip index order");
        }
    }

}
