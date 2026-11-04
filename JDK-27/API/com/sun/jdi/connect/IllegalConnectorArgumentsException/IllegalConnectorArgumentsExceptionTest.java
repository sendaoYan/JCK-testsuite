/*
 * @test
 * @summary com.sun.jdi.connect.IllegalConnectorArgumentsException conformance: white-box structure + black-box behavior.
 * @run main IllegalConnectorArgumentsExceptionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class IllegalConnectorArgumentsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.IllegalConnectorArgumentsException");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getDeclaredConstructors().length == 2, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("argumentNames"), "method argumentNames");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalConnectorArgumentsException a = new IllegalConnectorArgumentsException("boom", "foo");
        check("boom".equals(a.getMessage()), "message");
        check(a.argumentNames().size() == 1 && "foo".equals(a.argumentNames().get(0)), "argumentNames single");
        IllegalConnectorArgumentsException b = new IllegalConnectorArgumentsException("boom", java.util.Arrays.asList("foo", "bar"));
        check(b.argumentNames().size() == 2, "argumentNames list");
        check("foo".equals(b.argumentNames().get(0)) && "bar".equals(b.argumentNames().get(1)), "argumentNames order");
        try { throw new IllegalConnectorArgumentsException("x", java.util.Arrays.asList("a")); } catch (IllegalConnectorArgumentsException ex) { check(ex.argumentNames().size() == 1, "throw/catch"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
