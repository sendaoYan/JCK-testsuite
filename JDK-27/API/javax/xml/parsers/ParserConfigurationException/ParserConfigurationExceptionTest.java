/*
 * @test
 * @summary javax.xml.parsers.ParserConfigurationException conformance: white-box structure + black-box behavior.
 * @run main ParserConfigurationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ParserConfigurationExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.parsers.ParserConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");

        javax.xml.parsers.ParserConfigurationException e = new javax.xml.parsers.ParserConfigurationException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.ParserConfigurationException e = new javax.xml.parsers.ParserConfigurationException("cfg");
        check("cfg".equals(e.getMessage()), "message ctor");
        check(e.getCause() == null, "no cause by default");
        javax.xml.parsers.ParserConfigurationException e2 = new javax.xml.parsers.ParserConfigurationException();
        check(e2.getMessage() == null, "null message ctor");
        check(e.toString().startsWith("javax.xml.parsers.ParserConfigurationException"), "toString prefix");
        check(e instanceof java.lang.Exception, "is Exception");

    }

    // ---- Part 3: contract deepening (appended) ----
    static void testDeep() throws Exception {
        // exactly () and (String) constructors; no cause-carrying constructor exists
        check(javax.xml.parsers.ParserConfigurationException.class.getConstructors().length == 2,
              "exactly two public constructors");
        javax.xml.parsers.ParserConfigurationException eNull =
                new javax.xml.parsers.ParserConfigurationException(null);
        check(eNull.getMessage() == null, "null message allowed");
        check(java.io.Serializable.class.isAssignableFrom(
                javax.xml.parsers.ParserConfigurationException.class), "serializable via Exception");
        // initCause still works through Throwable machinery (no dedicated ctor)
        javax.xml.parsers.ParserConfigurationException e2 =
                new javax.xml.parsers.ParserConfigurationException("m");
        java.lang.IllegalStateException c = new java.lang.IllegalStateException("why");
        e2.initCause(c);
        check(e2.getCause() == c && "m".equals(e2.getMessage()), "initCause works without ctor support");
        // thrown by factory when a requested configuration cannot be provided
        boolean sawPce = false;
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            check(dbf.newDocumentBuilder() != null, "standard validating config supported");
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            sawPce = true;
        }
        check(!sawPce, "validating configuration supported");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
