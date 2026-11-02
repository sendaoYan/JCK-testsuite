/*
 * @test
 * @summary com.sun.security.auth.UnixPrincipal conformance: principal accessors.
 * @run main UnixPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class UnixPrincipalTest {
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
        com.sun.security.auth.UnixPrincipal p = new com.sun.security.auth.UnixPrincipal("user");
        check(p.getName().equals("user"), "name");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.UnixPrincipal("user")), "equals");
        check(p.hashCode() == new com.sun.security.auth.UnixPrincipal("user").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
