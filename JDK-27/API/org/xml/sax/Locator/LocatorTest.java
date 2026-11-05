/*
 * @test
 * @summary org.xml.sax.Locator conformance: white-box structure + black-box behavior.
 * @run main LocatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class LocatorTest {
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

        Class<?> c = Class.forName("org.xml.sax.Locator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getColumnNumber"), "method getColumnNumber");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.Locator a = new org.xml.sax.Locator(){
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
        };
        check(a != null, "anon impl of Locator");
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

    // ---- deepening round 2: behavioral Locator contract (live positions) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.Locator[] loc = new org.xml.sax.Locator[1];
        final int[] lineAtSecondDoc = new int[]{-1};
        final int[] colAtSecondDoc = new int[]{-1};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { loc[0] = l; }
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) {
                if (lineAtSecondDoc[0] < 0 && "a".equals(q)) { lineAtSecondDoc[0] = loc[0].getLineNumber(); colAtSecondDoc[0] = loc[0].getColumnNumber(); }
            }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>\n<a/></r>")));
        check(loc[0] != null, "setDocumentLocator delivers a live Locator");
        check(lineAtSecondDoc[0] == 2, "line number advances (element on line 2 reports 2)");
        check(colAtSecondDoc[0] > 0, "column number positive");
        check(loc[0].getPublicId() == null && loc[0].getSystemId() == null, "string ids default null for an anonymous stream");
        check(org.xml.sax.Locator.class.isInterface(), "Locator is an interface");
    }
    static int l0(org.xml.sax.Locator l) { return l.getLineNumber(); }
}
