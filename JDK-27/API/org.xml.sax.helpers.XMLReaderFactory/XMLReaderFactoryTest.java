/*
 * @test
 * @summary org.xml.sax.helpers.XMLReaderFactory conformance: white-box structure + black-box behavior.
 * @run main XMLReaderFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLReaderFactoryTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.XMLReaderFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "createXMLReader"), "method createXMLReader");

        org.xml.sax.XMLReader r = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        check(r != null, "createXMLReader");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.XMLReader r = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        check(r != null, "createXMLReader()");
        r.setContentHandler(new org.xml.sax.helpers.DefaultHandler());
        r.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r/>")));
        check(true, "created reader parses");
        check(org.xml.sax.helpers.XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser") != null, "createXMLReader(className)");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
