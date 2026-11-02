/*
 * @test
 * @summary com.sun.security.auth.UnixNumericUserPrincipal conformance: numeric user principal.
 * @run main UnixNumericUserPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class UnixNumericUserPrincipalTest {
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
        com.sun.security.auth.UnixNumericUserPrincipal p = new com.sun.security.auth.UnixNumericUserPrincipal(1000L);
        check(p.longValue() == 1000L, "longValue");
        check(p.getName().equals("1000"), "name");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.UnixNumericUserPrincipal(1000L)), "equals");
        com.sun.security.auth.UnixNumericUserPrincipal p2 = new com.sun.security.auth.UnixNumericUserPrincipal("2000");
        check(p2.longValue() == 2000L, "longValue from String");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
