/*
 * @test
 * @summary com.sun.nio.sctp.IllegalReceiveException conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.IllegalReceiveException");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("java.lang.IllegalStateException"), "extends IllegalStateException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(true, "no-arg and String constructors present");
        Set<String> f = fieldNames(c);
        check(f.contains("serialVersionUID"), "serialVersionUID field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.IllegalReceiveException e = new com.sun.nio.sctp.IllegalReceiveException("msg");
        check(e.getMessage().equals("msg"), "message round-trip");
        check(e instanceof java.lang.IllegalStateException, "instanceof IllegalStateException");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e.getCause() == null, "getCause null");

        com.sun.nio.sctp.IllegalReceiveException e2 = new com.sun.nio.sctp.IllegalReceiveException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.getCause() == null, "no-arg getCause null");

        // distinct instances carry independent messages
        check(!e.equals(e2), "distinct instances");
        check(e.getMessage().equals("msg") && e2.getMessage() == null, "messages independent");

        // can be caught as its superclass
        try {
            throw new com.sun.nio.sctp.IllegalReceiveException("boom");
        } catch (java.lang.IllegalStateException caught) {
            check(caught.getMessage().equals("boom"), "catch as IllegalStateException");
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
