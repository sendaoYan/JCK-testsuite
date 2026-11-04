/*
 * @test
 * @summary com.sun.jdi.ClassNotLoadedException conformance: white-box structure + black-box behavior.
 * @run main ClassNotLoadedExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class ClassNotLoadedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ClassNotLoadedException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getDeclaredConstructors().length == 2, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("className"), "method className");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassNotLoadedException a = new ClassNotLoadedException("java.lang.Foo");
        check(a.getMessage() == null, "single-arg ctor has no message");
        check("java.lang.Foo".equals(a.className()), "className");
        ClassNotLoadedException b = new ClassNotLoadedException("java.lang.Foo", "boom");
        check("boom".equals(b.getMessage()), "message2");
        check("java.lang.Foo".equals(b.className()), "className2");
        try { throw new ClassNotLoadedException("x", "y"); } catch (ClassNotLoadedException ex) { check("x".equals(ex.className()) && "y".equals(ex.getMessage()), "throw/catch"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
