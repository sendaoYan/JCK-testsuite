/*
 * @test
 * @summary com.sun.jdi.InvocationException conformance: white-box structure + black-box behavior.
 * @run main InvocationExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class InvocationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.InvocationException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("exception"), "method exception");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InvocationException e = new InvocationException(null);
        check(e.exception() == null, "exception null when constructed with null");
        check(e.getMessage() != null, "message non-null");
        try { throw new InvocationException(null); } catch (InvocationException ex) { check(ex.exception() == null, "throw/catch exception()"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
