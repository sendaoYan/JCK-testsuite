/*
 * @test
 * @summary com.sun.security.jgss.InquireType conformance: enum constants.
 * @run main InquireTypeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InquireTypeTest {
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
        com.sun.security.jgss.InquireType[] v = com.sun.security.jgss.InquireType.values();
        check(v.length == 6, "6 constants");
        check(com.sun.security.jgss.InquireType.KRB5_GET_SESSION_KEY.name().equals("KRB5_GET_SESSION_KEY"), "KRB5_GET_SESSION_KEY");
        check(com.sun.security.jgss.InquireType.valueOf("KRB5_GET_TKT_FLAGS") == com.sun.security.jgss.InquireType.KRB5_GET_TKT_FLAGS, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
