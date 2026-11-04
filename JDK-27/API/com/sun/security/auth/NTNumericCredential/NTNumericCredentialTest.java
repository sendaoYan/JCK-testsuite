/*
 * @test
 * @summary com.sun.security.auth.NTNumericCredential conformance: white-box structure + black-box behavior.
 * @run main NTNumericCredentialTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;

public class NTNumericCredentialTest {
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
        Class<?> c = load("com.sun.security.auth.NTNumericCredential");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"getToken","toString","equals","hashCode"}) check(m.contains(n), n);
    }
    static void testBlackBox() {
        com.sun.security.auth.NTNumericCredential c = new com.sun.security.auth.NTNumericCredential(12345L);
        check(c.getToken() == 12345L, "getToken");
        check(c.toString() != null, "toString non-null");
        check(c.toString().contains("12345"), "toString contains token");
        check(c.equals(c), "equals reflexive");
        com.sun.security.auth.NTNumericCredential c2 = new com.sun.security.auth.NTNumericCredential(12345L);
        check(c.equals(c2), "equals same token");
        check(c2.equals(c), "equals symmetric");
        check(c.hashCode() == c2.hashCode(), "hashCode consistent");
        check(!c.equals(new com.sun.security.auth.NTNumericCredential(99999L)), "equals different token");
        check(!c.equals(null), "equals null");
        check(!c.equals(Long.valueOf(12345L)), "equals other type");
        com.sun.security.auth.NTNumericCredential neg = new com.sun.security.auth.NTNumericCredential(-1L);
        check(neg.getToken() == -1L, "negative token preserved");
        check(c.getToken() == 12345L, "original token unchanged");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
