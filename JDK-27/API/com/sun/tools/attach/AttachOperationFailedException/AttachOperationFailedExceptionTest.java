/*
 * @test
 * @summary com.sun.tools.attach.AttachOperationFailedException conformance: white-box structure + black-box behavior.
 * @run main AttachOperationFailedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AttachOperationFailedExceptionTest {
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

    static void testWhiteBox() throws Exception {
        java.io.IOException cause = new java.io.IOException("cause");
        com.sun.tools.attach.AttachOperationFailedException e = new com.sun.tools.attach.AttachOperationFailedException("msg", cause);
        check(e.getMessage().equals("msg"), "message");
        check(e.getCause() == cause, "cause");
        check(e instanceof java.io.IOException, "extends IOException");
        new com.sun.tools.attach.AttachOperationFailedException("only msg");
    }

    static void testBlackBox() throws Exception {
        com.sun.tools.attach.AttachOperationFailedException e = new com.sun.tools.attach.AttachOperationFailedException("op failed");
        check(e.getMessage().equals("op failed"), "message");
        check(e instanceof java.io.IOException, "extends IOException");
        java.lang.Throwable cause = new java.lang.Throwable("cause");
        com.sun.tools.attach.AttachOperationFailedException e2 = new com.sun.tools.attach.AttachOperationFailedException("op", cause);
        check(e2.getCause() == cause, "getCause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
