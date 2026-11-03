/*
 * @test
 * @summary javax.xml.crypto.MarshalException conformance: white-box structure + black-box behavior.
 * @run main MarshalExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class MarshalExceptionTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.crypto.MarshalException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");

        javax.xml.crypto.MarshalException e = new javax.xml.crypto.MarshalException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.MarshalException e = new javax.xml.crypto.MarshalException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e.getCause() == null, "no cause by default");
        javax.xml.crypto.MarshalException e0 = new javax.xml.crypto.MarshalException();
        check(e0.getMessage() == null, "null message ctor");
        java.lang.RuntimeException cause = new java.lang.RuntimeException("r");
        javax.xml.crypto.MarshalException et = new javax.xml.crypto.MarshalException(cause);
        check(cause.toString().equals(et.getMessage()), "throwable ctor message");
        check(cause == et.getCause(), "throwable ctor cause");
        javax.xml.crypto.MarshalException est = new javax.xml.crypto.MarshalException("msg2", cause);
        check("msg2".equals(est.getMessage()) && est.getCause() == cause, "message+cause ctor");
        check(e instanceof java.lang.Exception, "exception family");
        check(e.toString().startsWith("javax.xml.crypto.MarshalException"), "toString prefix");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
