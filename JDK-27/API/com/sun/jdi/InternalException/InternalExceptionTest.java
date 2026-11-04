/*
 * @test
 * @summary com.sun.jdi.InternalException conformance: white-box structure + black-box behavior.
 * @run main InternalExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class InternalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.InternalException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass");
        check(c.getDeclaredConstructors().length == 4, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("errorCode"), "method errorCode");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InternalException a = new InternalException();
        check(a.getMessage() == null, "no-arg message");
        InternalException b = new InternalException("boom");
        check("boom".equals(b.getMessage()), "message");
        InternalException c = new InternalException(42);
        check(c.errorCode() == 42, "errorCode(int)");
        InternalException d = new InternalException("boom", 42);
        check("boom".equals(d.getMessage()), "message2");
        check(d.errorCode() == 42, "errorCode(String,int)");
        try { throw new InternalException("x", 7); } catch (InternalException ex) { check(ex.errorCode() == 7, "throw/catch errorCode"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
