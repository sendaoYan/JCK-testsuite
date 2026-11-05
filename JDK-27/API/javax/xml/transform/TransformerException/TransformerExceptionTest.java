/*
 * @test
 * @summary javax.xml.transform.TransformerException conformance: white-box structure + black-box behavior.
 * @run main TransformerExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.transform.TransformerException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getLocationAsString"), "method getLocationAsString");
        check(hasMethod(c, "getLocator"), "method getLocator");
        check(hasMethod(c, "getMessageAndLocation"), "method getMessageAndLocation");
        check(hasMethod(c, "initCause"), "method initCause");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");
        check(hasMethod(c, "setLocator"), "method setLocator");

        javax.xml.transform.TransformerException e = new javax.xml.transform.TransformerException("m", (javax.xml.transform.SourceLocator) null);
        check("m".equals(e.getMessage()), "msg");
        check(e.getLocator() == null, "locator null");
        check(e.getException() == null, "exception null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.TransformerException e = new javax.xml.transform.TransformerException("m");
        check("m".equals(e.getMessage()), "message ctor");
        check(e.getException() == null && e.getLocator() == null, "null exception/locator");
        java.lang.Throwable c = new RuntimeException("r");
        javax.xml.transform.TransformerException e2 = new javax.xml.transform.TransformerException("m", c);
        check(e2.getException() == c && e2.getCause() == c, "throwable ctor");
        javax.xml.transform.TransformerException e3 = new javax.xml.transform.TransformerException(c);
        check(e3.getException() == c && "java.lang.RuntimeException: r".equals(e3.getMessage()), "throwable-only ctor");
        e3.setLocator(null); check(e3.getLocator() == null, "setLocator null");
        check(e3.getLocationAsString() == null, "getLocationAsString null without locator");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorChains();
        testLocationStrings();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: full constructor chains + location contract
    static final javax.xml.transform.SourceLocator LOC = new javax.xml.transform.SourceLocator() {
        public String getPublicId() { return "pub"; }
        public String getSystemId() { return "http://base/x.xml"; }
        public int getLineNumber() { return 3; }
        public int getColumnNumber() { return 5; }
    };

    static void testConstructorChains() {
        // (String, SourceLocator)
        javax.xml.transform.TransformerException e1 =
            new javax.xml.transform.TransformerException("m1", LOC);
        check("m1".equals(e1.getMessage()), "(String,SourceLocator) message");
        check(e1.getLocator() == LOC, "(String,SourceLocator) keeps locator reference");
        // (String, Throwable)
        RuntimeException r = new RuntimeException("r");
        javax.xml.transform.TransformerException e2 =
            new javax.xml.transform.TransformerException("m2", r);
        check(e2.getException() == r && e2.getCause() == r,
              "(String,Throwable): getException and getCause both the wrapped throwable");
        // (Throwable)
        javax.xml.transform.TransformerException e3 =
            new javax.xml.transform.TransformerException(r);
        check(e3.getException() == r, "(Throwable): getException set");
        check("java.lang.RuntimeException: r".equals(e3.getMessage()),
              "(Throwable): message defaults to throwable toString");
        // (String, SourceLocator, Throwable)
        javax.xml.transform.TransformerException e4 =
            new javax.xml.transform.TransformerException("m4", LOC, r);
        check(e4.getLocator() == LOC && e4.getCause() == r,
              "(String,SourceLocator,Throwable): locator + cause both set");
        // initCause after construction
        javax.xml.transform.TransformerException e5 =
            new javax.xml.transform.TransformerException("m5");
        check(e5.getCause() == null, "cause initially null");
        e5.initCause(new java.io.IOException("io"));
        check("io".equals(e5.getCause().getMessage()), "initCause sets cause");
        // setLocator after construction
        javax.xml.transform.TransformerException e6 =
            new javax.xml.transform.TransformerException("m6");
        check(e6.getLocator() == null, "locator initially null");
        e6.setLocator(LOC);
        check(e6.getLocator() == LOC, "setLocator sets locator");
        // real compile failure wrapped: thrown exception is a TransformerException
        try {
            javax.xml.transform.TransformerFactory.newInstance().newTransformer(
                new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                    "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                  + "<xsl:template match=\"/\"><unclosed></xsl:stylesheet>")));
            check(false, "unclosed stylesheet must throw");
        } catch (javax.xml.transform.TransformerException e) {
            check(e.getMessage() != null && e.getMessage().contains("unclosed"),
                  "real compile failure: TransformerException message names the element");
        }
    }

    static void testLocationStrings() {
        javax.xml.transform.TransformerException e =
            new javax.xml.transform.TransformerException("m1", LOC);
        check("; SystemID: http://base/x.xml; Line#: 3; Column#: 5".equals(e.getLocationAsString()),
              "getLocationAsString exact form");
        check("m1; SystemID: http://base/x.xml; Line#: 3; Column#: 5".equals(e.getMessageAndLocation()),
              "getMessageAndLocation = message + location");
        javax.xml.transform.TransformerException n =
            new javax.xml.transform.TransformerException("no-loc");
        check(n.getLocationAsString() == null, "no locator -> getLocationAsString null");
        check("no-loc".equals(n.getMessageAndLocation()),
              "no locator -> getMessageAndLocation is bare message");
    }
}
