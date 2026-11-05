/*
 * @test
 * @summary org.xml.sax.ext.Locator2 conformance: white-box structure + black-box behavior.
 * @run main Locator2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class Locator2Test {
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

        Class<?> c = Class.forName("org.xml.sax.ext.Locator2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.Locator"), "interface org.xml.sax.Locator");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getXMLVersion"), "method getXMLVersion");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.Locator2 a = new org.xml.sax.ext.Locator2(){
            public java.lang.String getXMLVersion() { return "s"; }
            public java.lang.String getEncoding() { return "s"; }
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
        };
        check(a != null, "anon impl of Locator2");
        check("s".equals(a.getXMLVersion()), "getXMLVersion() returns fixed default");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Locator2 contract (version/encoding from a live parse) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.Locator.class.isAssignableFrom(org.xml.sax.ext.Locator2.class), "Locator2 extends Locator");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.Locator[] loc = new org.xml.sax.Locator[1];
        final String[] verAtLocator = new String[]{null};
        final String[] verAtElement = new String[]{null};
        final String[] encAtElement = new String[]{null};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) {
                loc[0] = l;
                if (l instanceof org.xml.sax.ext.Locator2) verAtLocator[0] = ((org.xml.sax.ext.Locator2) l).getXMLVersion();
            }
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) {
                if (loc[0] instanceof org.xml.sax.ext.Locator2) {
                    verAtElement[0] = ((org.xml.sax.ext.Locator2) loc[0]).getXMLVersion();
                    encAtElement[0] = ((org.xml.sax.ext.Locator2) loc[0]).getEncoding();
                }
            }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<?xml version='1.1'?><r/>")));
        check(loc[0] instanceof org.xml.sax.ext.Locator2, "JAXP reader hands out a Locator2");
        check("1.0".equals(verAtLocator[0]), "at setDocumentLocator time version still reports 1.0");
        check("1.1".equals(verAtElement[0]), "at first element the version reflects the declaration (1.1)");
        check(encAtElement[0] == null, "encoding null for a character-stream source");
    }
}
