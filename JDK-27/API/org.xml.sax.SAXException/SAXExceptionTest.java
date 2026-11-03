/*
 * @test
 * @summary org.xml.sax.SAXException conformance: white-box structure + black-box behavior.
 * @run main SAXExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXExceptionTest {
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

        Class<?> c = Class.forName("org.xml.sax.SAXException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getMessage"), "method getMessage");
        check(hasMethod(c, "toString"), "method toString");

        org.xml.sax.SAXException e = new org.xml.sax.SAXException("m", new Exception("c"));
        check("m".equals(e.getMessage()), "msg");
        check("c".equals(e.getException().getMessage()), "getException");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.SAXException e = new org.xml.sax.SAXException("m");
        check("m".equals(e.getMessage()) && e.getException() == null, "message ctor");
        java.lang.Exception c = new java.io.IOException("io");
        org.xml.sax.SAXException e2 = new org.xml.sax.SAXException(c);
        check(e2.getException() == c && e2.getCause() == c, "exception ctor");
        org.xml.sax.SAXException e3 = new org.xml.sax.SAXException("m", c);
        check("m".equals(e3.getMessage()) && e3.getException() == c, "message + exception");
        org.xml.sax.SAXException e4 = new org.xml.sax.SAXException();
        check(e4.getMessage() == null && e4.getException() == null, "no-arg");
        check(e.toString().startsWith("org.xml.sax.SAXException"), "toString prefix");
        check(e instanceof java.lang.Exception, "is Exception");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
