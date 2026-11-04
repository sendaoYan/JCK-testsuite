/*
 * @test
 * @summary org.xml.sax.SAXParseException conformance: white-box structure + black-box behavior.
 * @run main SAXParseExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXParseExceptionTest {
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

        Class<?> c = Class.forName("org.xml.sax.SAXParseException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.SAXException"), "superclass org.xml.sax.SAXException");
        check(hasMethod(c, "getColumnNumber"), "method getColumnNumber");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "toString"), "method toString");

        org.xml.sax.SAXParseException e = new org.xml.sax.SAXParseException("m", "pub", "sys", 5, 6);
        check("m".equals(e.getMessage()), "msg");
        check("pub".equals(e.getPublicId()), "publicId");
        check("sys".equals(e.getSystemId()), "systemId");
        check(e.getLineNumber() == 5, "line");
        check(e.getColumnNumber() == 6, "column");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.SAXParseException e = new org.xml.sax.SAXParseException("m", "pub", "sys", 10, 5);
        check("m".equals(e.getMessage()), "message");
        check("pub".equals(e.getPublicId()) && "sys".equals(e.getSystemId()), "ids");
        check(e.getLineNumber() == 10 && e.getColumnNumber() == 5, "line/column");
        check(e.toString().startsWith("org.xml.sax.SAXParseException"), "toString prefix");
        org.xml.sax.SAXParseException e2 = new org.xml.sax.SAXParseException("m2", "pub", "sys", 1, 2, new RuntimeException("c"));
        check("m2".equals(e2.getMessage()) && e2.getCause() != null, "6-arg ctor");
        check(e instanceof org.xml.sax.SAXException, "subclass of SAXException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
