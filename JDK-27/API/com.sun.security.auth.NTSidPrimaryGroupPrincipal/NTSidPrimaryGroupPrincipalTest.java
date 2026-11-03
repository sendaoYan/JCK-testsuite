/*
 * @test
 * @summary com.sun.security.auth.NTSidPrimaryGroupPrincipal conformance: white-box structure + black-box behavior.
 * @run main NTSidPrimaryGroupPrincipalTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;

public class NTSidPrimaryGroupPrincipalTest {
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
        Class<?> c = load("com.sun.security.auth.NTSidPrimaryGroupPrincipal");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("com.sun.security.auth.NTSid"), "extends NTSid");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"toString","equals"}) check(m.contains(n), n);
    }
    static void testBlackBox() {
        com.sun.security.auth.NTSidPrimaryGroupPrincipal p = new com.sun.security.auth.NTSidPrimaryGroupPrincipal("S-1-5-21-1-2-3-500");
        check(p instanceof com.sun.security.auth.NTSid, "is NTSid");
        check(p.getName().equals("S-1-5-21-1-2-3-500"), "getName");
        check(p.toString() != null, "toString non-null");
        check(p.toString().contains("S-1-5-21-1-2-3-500"), "toString contains sid");
        check(p.equals(p), "equals reflexive");
        com.sun.security.auth.NTSidPrimaryGroupPrincipal p2 = new com.sun.security.auth.NTSidPrimaryGroupPrincipal("S-1-5-21-1-2-3-500");
        check(p.equals(p2), "equals same sid");
        check(p2.equals(p), "equals symmetric");
        check(p.hashCode() == p2.hashCode(), "hashCode consistent");
        check(!p.equals(new com.sun.security.auth.NTSidPrimaryGroupPrincipal("S-1-5-21-9-9-9-999")), "equals different sid");
        check(!p.equals(null), "equals null");
        check(!p.equals(new com.sun.security.auth.NTSid("S-1-5-21-1-2-3-500")), "not equal to plain NTSid");
        check(p.equals(p), "still equals self");
        Subject s = new Subject();
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "subject contains principal");
        check(s.getPrincipals(com.sun.security.auth.NTSidPrimaryGroupPrincipal.class).contains(p), "subject typed principals");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
