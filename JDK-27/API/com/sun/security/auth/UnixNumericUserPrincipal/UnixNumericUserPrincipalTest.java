/*
 * @test
 * @summary com.sun.security.auth.UnixNumericUserPrincipal conformance: white-box structure + black-box behavior.
 * @run main UnixNumericUserPrincipalTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;

public class UnixNumericUserPrincipalTest {
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
        Class<?> c = load("com.sun.security.auth.UnixNumericUserPrincipal");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("java.security.Principal"), "implements Principal");
        check(interfaces(c).contains("java.io.Serializable"), "implements Serializable");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"getName","longValue","toString","equals","hashCode"}) check(m.contains(n), n);
    }
    static void testBlackBox() {
        com.sun.security.auth.UnixNumericUserPrincipal p = new com.sun.security.auth.UnixNumericUserPrincipal(1000L);
        check(p.longValue() == 1000L, "longValue from long ctor");
        check(p.getName().equals("1000"), "getName");
        check(p.toString() != null, "toString non-null");
        check(p.toString().contains("1000"), "toString contains uid");
        com.sun.security.auth.UnixNumericUserPrincipal viaString = new com.sun.security.auth.UnixNumericUserPrincipal("1000");
        check(p.equals(viaString), "long ctor equals string ctor");
        check(p.hashCode() == viaString.hashCode(), "hashCode consistent");
        check(p.equals(p), "equals reflexive");
        check(!p.equals(new com.sun.security.auth.UnixNumericUserPrincipal("2000")), "different uid not equal");
        check(!p.equals(null), "equals null");
        check(!p.equals("1000"), "equals other type");
        boolean npe = false; try { new com.sun.security.auth.UnixNumericUserPrincipal((String)null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null name throws NPE");
        Subject s = new Subject();
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "subject contains principal");
        check(s.getPrincipals(com.sun.security.auth.UnixNumericUserPrincipal.class).contains(p), "subject typed principals");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
