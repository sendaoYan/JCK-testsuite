/*
 * @test
 * @summary org.xml.sax.helpers.AttributesImpl conformance: white-box structure + black-box behavior.
 * @run main AttributesImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributesImplTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.AttributesImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.Attributes"), "interface org.xml.sax.Attributes");
        check(hasMethod(c, "addAttribute"), "method addAttribute");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getQName"), "method getQName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "removeAttribute"), "method removeAttribute");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
        check(hasMethod(c, "setLocalName"), "method setLocalName");
        check(hasMethod(c, "setQName"), "method setQName");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "setURI"), "method setURI");
        check(hasMethod(c, "setValue"), "method setValue");

        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        a.addAttribute("uri", "local", "q", "CDATA", "value");
        check(a.getLength() == 1, "length");
        check("value".equals(a.getValue(0)), "value");
        check("local".equals(a.getLocalName(0)), "localName");
        check("q".equals(a.getQName(0)), "qName");
        check("uri".equals(a.getURI(0)), "uri");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        check(a.getLength() == 0, "initially empty");
        a.addAttribute("urn:ns", "local", "p:local", "CDATA", "val");
        check(a.getLength() == 1, "addAttribute increments length");
        check("urn:ns".equals(a.getURI(0)), "getURI");
        check("local".equals(a.getLocalName(0)), "getLocalName");
        check("p:local".equals(a.getQName(0)), "getQName");
        check("CDATA".equals(a.getType(0)), "getType(int)");
        check("val".equals(a.getValue(0)), "getValue(int)");
        check(a.getIndex("p:local") == 0, "getIndex(qName)");
        check(a.getIndex("urn:ns", "local") == 0, "getIndex(uri,local)");
        check("val".equals(a.getValue("p:local")), "getValue(qName)");
        check("val".equals(a.getValue("urn:ns", "local")), "getValue(uri,local)");
        check(a.getType("p:local").equals("CDATA"), "getType(qName)");
        a.setAttribute(0, "urn:2", "l2", "p:l2", "CDATA", "v2");
        check("v2".equals(a.getValue(0)) && "l2".equals(a.getLocalName(0)), "setAttribute(int)");
        a.setValue(0, "v3"); check("v3".equals(a.getValue(0)), "setValue(int)");
        a.removeAttribute(0); check(a.getLength() == 0, "removeAttribute");
        a.addAttribute("", "", "q", "CDATA", "x");
        a.clear(); check(a.getLength() == 0, "clear");
        // copy ctor + setAttributes
        org.xml.sax.helpers.AttributesImpl src = new org.xml.sax.helpers.AttributesImpl();
        src.addAttribute("", "", "k", "CDATA", "v");
        org.xml.sax.helpers.AttributesImpl copy = new org.xml.sax.helpers.AttributesImpl(src);
        check(copy.getLength() == 1 && "v".equals(copy.getValue("k")), "copy ctor");
        org.xml.sax.helpers.AttributesImpl dst = new org.xml.sax.helpers.AttributesImpl();
        dst.setAttributes(src);
        check(dst.getLength() == 1, "setAttributes");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
