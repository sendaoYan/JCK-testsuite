/*
 * @test
 * @summary com.sun.security.auth.UnixNumericGroupPrincipal conformance: white-box structure + black-box behavior.
 * @run main UnixNumericGroupPrincipalTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;

public class UnixNumericGroupPrincipalTest {
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
        Class<?> c = load("com.sun.security.auth.UnixNumericGroupPrincipal");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("java.security.Principal"), "implements Principal");
        check(interfaces(c).contains("java.io.Serializable"), "implements Serializable");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"getName","longValue","isPrimaryGroup","toString","equals","hashCode"}) check(m.contains(n), n);
    }
    static void testBlackBox() {
        com.sun.security.auth.UnixNumericGroupPrincipal p = new com.sun.security.auth.UnixNumericGroupPrincipal(1000L, true);
        check(p.longValue() == 1000L, "longValue from long ctor");
        check(p.getName().equals("1000"), "getName");
        check(p.isPrimaryGroup(), "isPrimaryGroup true");
        check(p.toString() != null, "toString non-null");
        check(p.toString().contains("1000"), "toString contains gid");
        com.sun.security.auth.UnixNumericGroupPrincipal viaString = new com.sun.security.auth.UnixNumericGroupPrincipal("1000", true);
        check(p.equals(viaString), "long ctor equals string ctor");
        check(p.hashCode() == viaString.hashCode(), "hashCode consistent");
        com.sun.security.auth.UnixNumericGroupPrincipal supp = new com.sun.security.auth.UnixNumericGroupPrincipal("2000", false);
        check(supp.longValue() == 2000L, "longValue from string ctor");
        check(!supp.isPrimaryGroup(), "isPrimaryGroup false");
        check(!p.equals(supp), "different gid not equal");
        com.sun.security.auth.UnixNumericGroupPrincipal sameGidNotPrimary = new com.sun.security.auth.UnixNumericGroupPrincipal(1000L, false);
        check(!p.equals(sameGidNotPrimary), "primary flag participates in equals");
        check(!p.equals(null), "equals null");
        check(!p.equals("1000"), "equals other type");
        boolean npe = false; try { new com.sun.security.auth.UnixNumericGroupPrincipal((String)null, true); } catch (NullPointerException e) { npe = true; }
        check(npe, "null name throws NPE");
        Subject s = new Subject();
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "subject contains principal");
        check(s.getPrincipals(com.sun.security.auth.UnixNumericGroupPrincipal.class).contains(p), "subject typed principals");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
