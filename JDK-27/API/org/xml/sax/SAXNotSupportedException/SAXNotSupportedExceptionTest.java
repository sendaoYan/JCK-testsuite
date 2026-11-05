/*
 * @test
 * @summary org.xml.sax.SAXNotSupportedException conformance: white-box structure + black-box behavior.
 * @run main SAXNotSupportedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXNotSupportedExceptionTest {
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

        Class<?> c = Class.forName("org.xml.sax.SAXNotSupportedException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.SAXException"), "superclass org.xml.sax.SAXException");

        org.xml.sax.SAXNotSupportedException e = new org.xml.sax.SAXNotSupportedException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.SAXNotSupportedException e = new org.xml.sax.SAXNotSupportedException("m");
        check("m".equals(e.getMessage()) && e instanceof org.xml.sax.SAXException, "message + subclass");
        org.xml.sax.SAXNotSupportedException e2 = new org.xml.sax.SAXNotSupportedException();
        check(e2.getMessage() == null, "no-arg");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral SAXNotSupportedException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.xml.sax.SAXNotSupportedException e = new org.xml.sax.SAXNotSupportedException("cannot do that");
        check("cannot do that".equals(e.getMessage()), "(String) constructor keeps the message");
        check(org.xml.sax.SAXException.class.isAssignableFrom(org.xml.sax.SAXNotSupportedException.class), "extends SAXException");
        check(!org.xml.sax.SAXNotRecognizedException.class.isInstance(e), "distinct from SAXNotRecognizedException");
        // real trigger: recognized-but-unsupported feature (is-standalone is read-only)
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        try { xr.setFeature("http://xml.org/sax/features/is-standalone", true); check(false, "read-only feature must throw"); }
        catch (org.xml.sax.SAXNotSupportedException ex) { check(true, "setFeature(is-standalone) -> SAXNotSupportedException"); }
        // recognized property with a wrong-typed value
        try { xr.setProperty("http://xml.org/sax/properties/lexical-handler", "not-a-handler"); check(false, "wrong-typed property value must throw"); }
        catch (org.xml.sax.SAXNotSupportedException ex) { check(true, "setProperty(lexical-handler, String) -> SAXNotSupportedException"); }
    }
}
