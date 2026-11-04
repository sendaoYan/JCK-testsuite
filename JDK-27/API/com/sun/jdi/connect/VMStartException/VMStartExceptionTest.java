/*
 * @test
 * @summary com.sun.jdi.connect.VMStartException conformance: white-box structure + black-box behavior.
 * @run main VMStartExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class VMStartExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.VMStartException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getDeclaredConstructors().length == 2, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("process"), "method process");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VMStartException a = new VMStartException((java.lang.Process) null);
        check(a.process() == null, "process null");
        check(a.getMessage() == null, "single-arg ctor has no message");
        VMStartException b = new VMStartException("boom", (java.lang.Process) null);
        check("boom".equals(b.getMessage()), "message2");
        check(b.process() == null, "process null2");
        try { throw new VMStartException("x", (java.lang.Process) null); } catch (VMStartException ex) { check(ex.process() == null, "throw/catch"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
