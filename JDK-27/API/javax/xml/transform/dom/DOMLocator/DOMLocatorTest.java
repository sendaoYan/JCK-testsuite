/*
 * @test
 * @summary javax.xml.transform.dom.DOMLocator conformance: white-box structure + black-box behavior.
 * @run main DOMLocatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMLocatorTest {
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

        Class<?> c = Class.forName("javax.xml.transform.dom.DOMLocator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.transform.SourceLocator"), "interface javax.xml.transform.SourceLocator");
        check(hasMethod(c, "getOriginatingNode"), "method getOriginatingNode");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.dom.DOMLocator a = new javax.xml.transform.dom.DOMLocator(){
            public org.w3c.dom.Node getOriginatingNode() { return null; }
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
        };
        check(a != null, "anon impl of DOMLocator");
        check(a.getOriginatingNode() == null, "getOriginatingNode() returns fixed default");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAnonImpl();
        testAsSourceLocator();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: behavior + DOMLocator usable as SourceLocator in exceptions
    static void testAnonImpl() throws Exception {
        final org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        javax.xml.transform.dom.DOMLocator loc = new javax.xml.transform.dom.DOMLocator() {
            public org.w3c.dom.Node getOriginatingNode() { return doc; }
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 2; }
            public int getColumnNumber() { return 4; }
        };
        check(loc.getOriginatingNode() == doc, "getOriginatingNode returns the DOM node");
        check("pub".equals(loc.getPublicId()), "getPublicId");
        check("http://base/x.xml".equals(loc.getSystemId()), "getSystemId");
        check(loc.getLineNumber() == 2 && loc.getColumnNumber() == 4, "line/column");
    }

    static void testAsSourceLocator() {
        javax.xml.transform.dom.DOMLocator loc = new javax.xml.transform.dom.DOMLocator() {
            public org.w3c.dom.Node getOriginatingNode() { return null; }
            public String getPublicId() { return null; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 6; }
            public int getColumnNumber() { return 9; }
        };
        check(loc instanceof javax.xml.transform.SourceLocator,
              "DOMLocator is usable wherever a SourceLocator is required");
        javax.xml.transform.TransformerException e =
            new javax.xml.transform.TransformerException("m", loc);
        check(e.getLocator() == loc, "TransformerException keeps the DOMLocator reference");
        check(e.getLocationAsString() != null && e.getLocationAsString().contains("6"),
              "DOMLocator rendered in getLocationAsString");
    }
}
