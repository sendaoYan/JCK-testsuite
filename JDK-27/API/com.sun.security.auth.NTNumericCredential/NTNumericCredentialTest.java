/*
 * @test
 * @summary com.sun.security.auth.NTNumericCredential conformance: token accessor.
 * @run main NTNumericCredentialTest
 */

import java.lang.reflect.*;
import java.util.*;

public class NTNumericCredentialTest {
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
        com.sun.security.auth.NTNumericCredential c = new com.sun.security.auth.NTNumericCredential(12345L);
        check(c.getToken() == 12345L, "token");
        check(c.toString() != null, "toString");
        check(c.equals(new com.sun.security.auth.NTNumericCredential(12345L)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
