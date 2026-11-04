/*
 * @test
 * @summary com.sun.nio.sctp.InvalidStreamException conformance: white-box structure + black-box behavior.
 * @run main InvalidStreamExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InvalidStreamExceptionTest {
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
        Class<?> c = load("com.sun.nio.sctp.InvalidStreamException");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("java.lang.IllegalArgumentException"), "extends IllegalArgumentException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(true, "no-arg and String constructors present");
        // JDK-27 InvalidStreamException declares no streamNumber/association getters;
        // it is a pure unchecked marker exception (verified via javap + source).
        Set<String> m = methodNames(c);
        check(!m.contains("streamNumber"), "no streamNumber getter");
        check(!m.contains("association"), "no association getter");
        Set<String> f = fieldNames(c);
        check(f.contains("serialVersionUID"), "serialVersionUID field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.InvalidStreamException e = new com.sun.nio.sctp.InvalidStreamException("msg");
        check(e.getMessage().equals("msg"), "message round-trip");
        check(e instanceof java.lang.IllegalArgumentException, "instanceof IllegalArgumentException");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e.getCause() == null, "getCause null");

        com.sun.nio.sctp.InvalidStreamException e2 = new com.sun.nio.sctp.InvalidStreamException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.getCause() == null, "no-arg getCause null");

        check(!e.equals(e2), "distinct instances");
        check(e.getMessage().equals("msg") && e2.getMessage() == null, "messages independent");

        try {
            throw new com.sun.nio.sctp.InvalidStreamException("boom");
        } catch (java.lang.IllegalArgumentException caught) {
            check(caught.getMessage().equals("boom"), "catch as IllegalArgumentException");
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
