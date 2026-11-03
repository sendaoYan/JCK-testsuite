/*
 * @test
 * @summary com.sun.security.auth.LdapPrincipal conformance: white-box structure + black-box behavior.
 * @run main LdapPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.InvalidNameException;
import javax.security.auth.Subject;

public class LdapPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.LdapPrincipal");
        check(!c.isInterface(), "not an interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(interfaces(c).contains("java.security.Principal"), "implements Principal");
        check(interfaces(c).contains("java.io.Serializable"), "implements Serializable");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"getName","toString","equals","hashCode"}) check(m.contains(n), n);
    }

    static void testBlackBox() throws Exception {
        String dn = "cn=foo,dc=example,dc=com";
        com.sun.security.auth.LdapPrincipal p = new com.sun.security.auth.LdapPrincipal(dn);
        check(p.getName().equals(dn), "getName returns original string");
        check(p.toString() != null, "toString non-null");
        check(!p.toString().isEmpty(), "toString non-empty");
        check(p.equals(p), "equals reflexive");

        // LdapName comparison is insensitive to the spaces around the commas.
        com.sun.security.auth.LdapPrincipal spaced = new com.sun.security.auth.LdapPrincipal("cn=foo, dc=example, dc=com");
        check(p.equals(spaced), "equals same DN ignoring separators spacing");
        check(spaced.equals(p), "equals symmetric");
        check(p.hashCode() == spaced.hashCode(), "hashCode consistent");

        check(!p.equals(new com.sun.security.auth.LdapPrincipal("cn=bar,dc=example,dc=com")), "different DN not equal");
        check(!p.equals(null), "equals null");
        check(!p.equals(dn), "equals other type");

        boolean npe = false;
        try { new com.sun.security.auth.LdapPrincipal((String)null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null name throws NPE");

        boolean ine = false;
        try { new com.sun.security.auth.LdapPrincipal("foo"); } catch (InvalidNameException e) { ine = true; }
        check(ine, "invalid DN throws InvalidNameException");

        Subject s = new Subject();
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "subject contains principal");
        check(s.getPrincipals(com.sun.security.auth.LdapPrincipal.class).contains(p), "subject typed principals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
