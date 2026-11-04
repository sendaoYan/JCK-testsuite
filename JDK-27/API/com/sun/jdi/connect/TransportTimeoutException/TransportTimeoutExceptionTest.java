/*
 * @test
 * @summary com.sun.jdi.connect.TransportTimeoutException conformance: white-box structure + black-box behavior.
 * @run main TransportTimeoutExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class TransportTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.TransportTimeoutException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "superclass");
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
        TransportTimeoutException a = new TransportTimeoutException();
        check(a.getMessage() == null, "no-arg message null");
        TransportTimeoutException b = new TransportTimeoutException("boom");
        check("boom".equals(b.getMessage()), "message round-trip");
        try { throw new TransportTimeoutException("x"); } catch (TransportTimeoutException ex) { check("x".equals(ex.getMessage()), "throw/catch"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
