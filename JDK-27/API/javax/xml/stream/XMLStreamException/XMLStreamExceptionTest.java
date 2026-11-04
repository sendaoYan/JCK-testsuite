/*
 * @test
 * @summary javax.xml.stream.XMLStreamException conformance: white-box structure + black-box behavior.
 * @run main XMLStreamExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLStreamExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLStreamException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getNestedException"), "method getNestedException");

        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("m");
        check("m".equals(e.getMessage()), "msg");
        check(e.getLocation() == null, "location null");
        check(e.getNestedException() == null, "nested null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("m");
        check("m".equals(e.getMessage()) && e.getNestedException() == null, "message ctor");
        java.lang.Throwable c = new RuntimeException("r");
        javax.xml.stream.XMLStreamException e2 = new javax.xml.stream.XMLStreamException("m", c);
        check(e2.getNestedException() == c && e2.getCause() == c, "message + throwable");
        javax.xml.stream.XMLStreamException e3 = new javax.xml.stream.XMLStreamException(c);
        check(e3.getNestedException() == c && e3.getLocation() == null, "throwable ctor");
        javax.xml.stream.XMLStreamException e4 = new javax.xml.stream.XMLStreamException();
        check(e4.getMessage() == null, "no-arg");
        check(e instanceof java.lang.Exception, "is Exception");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
