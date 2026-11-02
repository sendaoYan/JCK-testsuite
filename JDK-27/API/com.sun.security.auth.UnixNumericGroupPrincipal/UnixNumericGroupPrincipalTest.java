/*
 * @test
 * @summary com.sun.security.auth.UnixNumericGroupPrincipal conformance: numeric group principal.
 * @run main UnixNumericGroupPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class UnixNumericGroupPrincipalTest {
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
        com.sun.security.auth.UnixNumericGroupPrincipal p = new com.sun.security.auth.UnixNumericGroupPrincipal(1000L, true);
        check(p.longValue() == 1000L, "longValue");
        check(p.getName().equals("1000"), "name");
        check(p.isPrimaryGroup(), "isPrimaryGroup");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.UnixNumericGroupPrincipal(1000L, true)), "equals");
        check(p.hashCode() == new com.sun.security.auth.UnixNumericGroupPrincipal(1000L, true).hashCode(), "hashCode");
        com.sun.security.auth.UnixNumericGroupPrincipal p2 = new com.sun.security.auth.UnixNumericGroupPrincipal("2000", false);
        check(p2.longValue() == 2000L, "longValue from String");
        check(!p2.isPrimaryGroup(), "not primary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
