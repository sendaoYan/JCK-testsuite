/*
 * @test
 * @summary com.sun.security.auth.LdapPrincipal conformance: principal accessors.
 * @run main LdapPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class LdapPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        com.sun.security.auth.LdapPrincipal p = new com.sun.security.auth.LdapPrincipal("cn=foo,dc=example,dc=com");
        check(p.getName().equals("cn=foo,dc=example,dc=com"), "name");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.LdapPrincipal("cn=foo,dc=example,dc=com")), "equals");
        check(p.hashCode() == new com.sun.security.auth.LdapPrincipal("cn=foo,dc=example,dc=com").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
