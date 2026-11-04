/*
 * @test
 * @summary org.xml.sax.XMLReader conformance: white-box structure + black-box behavior.
 * @run main XMLReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLReaderTest {
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

        Class<?> c = Class.forName("org.xml.sax.XMLReader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getContentHandler"), "method getContentHandler");
        check(hasMethod(c, "getDTDHandler"), "method getDTDHandler");
        check(hasMethod(c, "getEntityResolver"), "method getEntityResolver");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "setContentHandler"), "method setContentHandler");
        check(hasMethod(c, "setDTDHandler"), "method setDTDHandler");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.XMLReader a = new org.xml.sax.XMLReader(){
            public boolean getFeature(java.lang.String p0) { return true; }
            public void setFeature(java.lang.String p0, boolean p1) { }
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
            public void setProperty(java.lang.String p0, java.lang.Object p1) { }
            public void setEntityResolver(org.xml.sax.EntityResolver p0) { }
            public org.xml.sax.EntityResolver getEntityResolver() { return null; }
            public void setDTDHandler(org.xml.sax.DTDHandler p0) { }
            public org.xml.sax.DTDHandler getDTDHandler() { return null; }
            public void setContentHandler(org.xml.sax.ContentHandler p0) { }
            public org.xml.sax.ContentHandler getContentHandler() { return null; }
            public void setErrorHandler(org.xml.sax.ErrorHandler p0) { }
            public org.xml.sax.ErrorHandler getErrorHandler() { return null; }
            public void parse(org.xml.sax.InputSource p0) { }
            public void parse(java.lang.String p0) { }
        };
        check(a != null, "anon impl of XMLReader");
        check(a.getFeature("s") == true, "getFeature() returns fixed default");
        a.setFeature("s", true);
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
        a.setProperty("s", (java.lang.Object) null);
        a.setEntityResolver((org.xml.sax.EntityResolver) null);
        check(a.getEntityResolver() == null, "getEntityResolver() returns fixed default");
        a.setDTDHandler((org.xml.sax.DTDHandler) null);
        check(a.getDTDHandler() == null, "getDTDHandler() returns fixed default");
        a.setContentHandler((org.xml.sax.ContentHandler) null);
        check(a.getContentHandler() == null, "getContentHandler() returns fixed default");
        a.setErrorHandler((org.xml.sax.ErrorHandler) null);
        check(a.getErrorHandler() == null, "getErrorHandler() returns fixed default");
        a.parse((org.xml.sax.InputSource) null);
        a.parse("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
