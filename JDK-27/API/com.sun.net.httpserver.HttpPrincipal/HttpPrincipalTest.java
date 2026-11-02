/*
 * @test
 * @summary com.sun.net.httpserver.HttpPrincipal conformance: principal accessors.
 * @run main HttpPrincipalTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpPrincipalTest {
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
        com.sun.net.httpserver.HttpPrincipal p = new com.sun.net.httpserver.HttpPrincipal("user", "realm");
        check(p.getName().equals("realm:user"), "name == realm:user");
        check(p.getUsername().equals("user"), "username");
        check(p.getRealm().equals("realm"), "realm");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.net.httpserver.HttpPrincipal("user", "realm")), "equals");
        check(p.hashCode() == new com.sun.net.httpserver.HttpPrincipal("user", "realm").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
