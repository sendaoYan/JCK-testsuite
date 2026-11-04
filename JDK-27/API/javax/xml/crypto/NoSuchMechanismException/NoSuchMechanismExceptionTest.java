/*
 * @test
 * @summary javax.xml.crypto.NoSuchMechanismException conformance: white-box structure + black-box behavior.
 * @run main NoSuchMechanismExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NoSuchMechanismExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.NoSuchMechanismException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");

        javax.xml.crypto.NoSuchMechanismException e = new javax.xml.crypto.NoSuchMechanismException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.NoSuchMechanismException e = new javax.xml.crypto.NoSuchMechanismException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e.getCause() == null, "no cause by default");
        javax.xml.crypto.NoSuchMechanismException e0 = new javax.xml.crypto.NoSuchMechanismException();
        check(e0.getMessage() == null, "null message ctor");
        java.lang.RuntimeException cause = new java.lang.RuntimeException("r");
        javax.xml.crypto.NoSuchMechanismException et = new javax.xml.crypto.NoSuchMechanismException(cause);
        check(cause.toString().equals(et.getMessage()), "throwable ctor message");
        check(cause == et.getCause(), "throwable ctor cause");
        javax.xml.crypto.NoSuchMechanismException est = new javax.xml.crypto.NoSuchMechanismException("msg2", cause);
        check("msg2".equals(est.getMessage()) && est.getCause() == cause, "message+cause ctor");
        check(e instanceof java.lang.RuntimeException, "exception family");
        check(e.toString().startsWith("javax.xml.crypto.NoSuchMechanismException"), "toString prefix");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
