/*
 * @test
 * @summary com.sun.jdi.InvalidCodeIndexException conformance: white-box structure + black-box behavior.
 * @run main InvalidCodeIndexExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class InvalidCodeIndexExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.InvalidCodeIndexException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass");
        check(c.getDeclaredConstructors().length == 2, "constructor count");
        Set<String> dm = methodNames(c);
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InvalidCodeIndexException a = new InvalidCodeIndexException();
        check(a.getMessage() == null, "no-arg message null");
        InvalidCodeIndexException b = new InvalidCodeIndexException("boom");
        check("boom".equals(b.getMessage()), "message round-trip");
        try { throw new InvalidCodeIndexException("x"); } catch (InvalidCodeIndexException ex) { check("x".equals(ex.getMessage()), "throw/catch"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
