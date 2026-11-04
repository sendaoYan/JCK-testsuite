/*
 * @test
 * @summary javax.xml.parsers.FactoryConfigurationError conformance: white-box structure + black-box behavior.
 * @run main FactoryConfigurationErrorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class FactoryConfigurationErrorTest {
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

        Class<?> c = Class.forName("javax.xml.parsers.FactoryConfigurationError");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getMessage"), "method getMessage");

        javax.xml.parsers.FactoryConfigurationError e = new javax.xml.parsers.FactoryConfigurationError(new Exception("x"));
        check("x".equals(e.getException().getMessage()), "getException");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.FactoryConfigurationError e = new javax.xml.parsers.FactoryConfigurationError("why");
        check("why".equals(e.getMessage()), "message ctor");
        java.lang.Exception cause = new java.io.IOException("io");
        javax.xml.parsers.FactoryConfigurationError e2 = new javax.xml.parsers.FactoryConfigurationError(cause);
        check(e2.getException() == cause && e2.getCause() == cause, "exception ctor exposes cause");
        check("java.io.IOException: io".equals(e2.getMessage()), "message derived from exception");
        javax.xml.parsers.FactoryConfigurationError e3 = new javax.xml.parsers.FactoryConfigurationError(cause, "extra");
        check("extra".equals(e3.getMessage()) && e3.getException() == cause, "(exception, message) ctor");
        javax.xml.parsers.FactoryConfigurationError e4 = new javax.xml.parsers.FactoryConfigurationError();
        check(e4.getMessage() == null && e4.getException() == null, "no-arg ctor");
        check(e instanceof java.lang.Error, "is Error");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
