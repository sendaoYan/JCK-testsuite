/*
 * @test
 * @summary com.sun.security.jgss.ExtendedGSSContext conformance: extended GSS context interface.
 * @run main ExtendedGSSContextTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedGSSContextTest {
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
        Class<?> c = load("com.sun.security.jgss.ExtendedGSSContext");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("org.ietf.jgss.GSSContext"), "extends GSSContext");
        Set<String> m = methodNames(c);
        check(m.contains("inquireSecContext"), "inquireSecContext");
        check(m.contains("requestDelegPolicy"), "requestDelegPolicy");
        check(m.contains("getDelegPolicyState"), "getDelegPolicyState");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
