/*
 * @test
 * @summary org.xml.sax.SAXNotRecognizedException conformance: white-box structure + black-box behavior.
 * @run main SAXNotRecognizedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXNotRecognizedExceptionTest {
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

        Class<?> c = Class.forName("org.xml.sax.SAXNotRecognizedException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.SAXException"), "superclass org.xml.sax.SAXException");

        org.xml.sax.SAXNotRecognizedException e = new org.xml.sax.SAXNotRecognizedException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.SAXNotRecognizedException e = new org.xml.sax.SAXNotRecognizedException("m");
        check("m".equals(e.getMessage()) && e instanceof org.xml.sax.SAXException, "message + subclass");
        org.xml.sax.SAXNotRecognizedException e2 = new org.xml.sax.SAXNotRecognizedException();
        check(e2.getMessage() == null, "no-arg");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral SAXNotRecognizedException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.xml.sax.SAXNotRecognizedException e = new org.xml.sax.SAXNotRecognizedException("no such id");
        check("no such id".equals(e.getMessage()), "(String) constructor keeps the message");
        check(org.xml.sax.SAXException.class.isAssignableFrom(org.xml.sax.SAXNotRecognizedException.class), "extends SAXException");
        check(new org.xml.sax.SAXNotRecognizedException().getMessage() == null, "no-arg constructor: null message");
        // real trigger: unknown feature id on a live reader
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        try { xr.setFeature("http://xml.org/sax/features/no-such-feature", true); check(false, "unknown feature must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(ex.getMessage() != null, "setFeature(unknown) -> SAXNotRecognizedException with message"); }
        try { xr.getFeature("http://xml.org/sax/features/no-such-feature"); check(false, "unknown feature get must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(true, "getFeature(unknown) -> SAXNotRecognizedException"); }
        try { xr.setProperty("http://xml.org/sax/properties/no-such-property", new Object()); check(false, "unknown property must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(true, "setProperty(unknown) -> SAXNotRecognizedException"); }
    }
}
