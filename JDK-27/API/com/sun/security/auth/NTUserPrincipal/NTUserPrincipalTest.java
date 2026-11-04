/*
 * @test
 * @summary com.sun.security.auth.NTUserPrincipal conformance: white-box structure + black-box behavior.
 * @run main NTUserPrincipalTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;

public class NTUserPrincipalTest {
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
        Class<?> c = load("com.sun.security.auth.NTUserPrincipal");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("java.security.Principal"), "implements Principal");
        check(interfaces(c).contains("java.io.Serializable"), "implements Serializable");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"getName","toString","equals","hashCode"}) check(m.contains(n), n);
    }
    static void testBlackBox() {
        com.sun.security.auth.NTUserPrincipal p = new com.sun.security.auth.NTUserPrincipal("jck-user");
        check(p.getName().equals("jck-user"), "getName");
        check(p.toString() != null, "toString non-null");
        check(p.toString().contains("jck-user"), "toString contains name");
        check(p.equals(p), "equals reflexive");
        com.sun.security.auth.NTUserPrincipal p2 = new com.sun.security.auth.NTUserPrincipal("jck-user");
        check(p.equals(p2), "equals same name");
        check(p2.equals(p), "equals symmetric");
        check(p.hashCode() == p2.hashCode(), "hashCode consistent");
        check(!p.equals(new com.sun.security.auth.NTUserPrincipal("other")), "equals different name");
        check(!p.equals(null), "equals null");
        check(!p.equals("jck-user"), "equals other type");
        Subject s = new Subject();
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "subject contains principal");
        check(s.getPrincipals(com.sun.security.auth.NTUserPrincipal.class).contains(p), "subject typed principals");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
