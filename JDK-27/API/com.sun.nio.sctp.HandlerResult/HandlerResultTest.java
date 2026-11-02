/*
 * @test
 * @summary com.sun.nio.sctp.HandlerResult conformance: enum constants.
 * @run main HandlerResultTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HandlerResultTest {
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
        com.sun.nio.sctp.HandlerResult[] v = com.sun.nio.sctp.HandlerResult.values();
        check(v.length == 2, "2 constants");
        check(com.sun.nio.sctp.HandlerResult.CONTINUE.name().equals("CONTINUE"), "CONTINUE");
        check(com.sun.nio.sctp.HandlerResult.valueOf("RETURN") == com.sun.nio.sctp.HandlerResult.RETURN, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
