/*
 * @test
 * @summary com.sun.nio.sctp.IllegalReceiveException conformance: exception.
 * @run main IllegalReceiveExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class IllegalReceiveExceptionTest {
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
        com.sun.nio.sctp.IllegalReceiveException e = new com.sun.nio.sctp.IllegalReceiveException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof java.lang.IllegalStateException, "extends IllegalStateException");
        new com.sun.nio.sctp.IllegalReceiveException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
