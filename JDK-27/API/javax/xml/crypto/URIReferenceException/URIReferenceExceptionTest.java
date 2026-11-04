/*
 * @test
 * @summary javax.xml.crypto.URIReferenceException conformance: white-box structure + black-box behavior.
 * @run main URIReferenceExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class URIReferenceExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.URIReferenceException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getURIReference"), "method getURIReference");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");

        javax.xml.crypto.URIReferenceException e = new javax.xml.crypto.URIReferenceException("m", new RuntimeException("c"));
        check("m".equals(e.getMessage()), "msg");
        check(e.getCause() != null, "cause");
        check(e.getURIReference() == null, "uriReference null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.URIReferenceException e = new javax.xml.crypto.URIReferenceException("msg");
        check("msg".equals(e.getMessage()) && e.getCause() == null, "message ctor");
        javax.xml.crypto.URIReferenceException e0 = new javax.xml.crypto.URIReferenceException();
        check(e0.getMessage() == null, "null message ctor");
        java.lang.RuntimeException cause = new java.lang.RuntimeException("r");
        javax.xml.crypto.URIReferenceException et = new javax.xml.crypto.URIReferenceException(cause);
        check(cause.toString().equals(et.getMessage()) && et.getCause() == cause, "throwable ctor");
        javax.xml.crypto.URIReference ref = new javax.xml.crypto.URIReference() {
            public String getURI() { return "urn:x"; }
            public String getType() { return null; }
        };
        javax.xml.crypto.URIReferenceException u = new javax.xml.crypto.URIReferenceException("m", cause, ref);
        check(u.getURIReference() == ref && u.getCause() == cause, "3-arg ctor");
        check(e.getURIReference() == null, "default URIReference null");
        check(e.toString().startsWith("javax.xml.crypto.URIReferenceException"), "toString prefix");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
