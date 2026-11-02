/*
 * @test
 * @summary com.sun.security.auth.NTSidUserPrincipal conformance: principal accessors.
 * @run main NTSidUserPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class NTSidUserPrincipalTest {
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
        com.sun.security.auth.NTSidUserPrincipal p = new com.sun.security.auth.NTSidUserPrincipal("S-1-5-21-1-2-3-500");
        check(p instanceof com.sun.security.auth.NTSid, "extends NTSid");
        check(p.getName().equals("S-1-5-21-1-2-3-500"), "name");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.NTSidUserPrincipal("S-1-5-21-1-2-3-500")), "equals");
        check(p.hashCode() == new com.sun.security.auth.NTSidUserPrincipal("S-1-5-21-1-2-3-500").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
