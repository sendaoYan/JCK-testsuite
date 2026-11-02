/*
 * @test
 * @summary com.sun.security.jgss.InquireSecContextPermission conformance: permission.
 * @run main InquireSecContextPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InquireSecContextPermissionTest {
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
        com.sun.security.jgss.InquireSecContextPermission p = new com.sun.security.jgss.InquireSecContextPermission("KRB5_GET_SESSION_KEY");
        check(p.getName().equals("KRB5_GET_SESSION_KEY"), "name");
        check(p instanceof java.security.BasicPermission, "extends BasicPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
