/*
 * @test
 * @summary javax.xml.parsers.SAXParserFactory conformance: white-box structure + black-box behavior.
 * @run main SAXParserFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXParserFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.parsers.SAXParserFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newDefaultNSInstance"), "method newDefaultNSInstance");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newNSInstance"), "method newNSInstance");
        check(hasMethod(c, "newSAXParser"), "method newSAXParser");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setNamespaceAware"), "method setNamespaceAware");
        check(hasMethod(c, "setSchema"), "method setSchema");
        check(hasMethod(c, "setValidating"), "method setValidating");
        check(hasMethod(c, "setXIncludeAware"), "method setXIncludeAware");

        javax.xml.parsers.SAXParserFactory f = javax.xml.parsers.SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        check(f.isNamespaceAware(), "namespaceAware");
        check(f.newSAXParser() != null, "newSAXParser");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.SAXParserFactory f = javax.xml.parsers.SAXParserFactory.newInstance();
        check(!f.isNamespaceAware() && !f.isValidating(), "sax factory defaults");
        f.setNamespaceAware(true); f.setValidating(false); f.setXIncludeAware(true);
        check(f.isNamespaceAware() && !f.isValidating() && f.isXIncludeAware(), "sax toggles");
        f.setNamespaceAware(false); check(!f.isNamespaceAware(), "setNamespaceAware(false)");
        f.setNamespaceAware(true);
        javax.xml.parsers.SAXParser p = f.newSAXParser();
        check(p != null && p.isNamespaceAware(), "parser reflects namespace aware");
        check(!p.isValidating(), "parser not validating");
        check(f.getSchema() == null && p.getSchema() == null, "schema default null");
        f.setFeature("http://xml.org/sax/features/namespaces", true);
        check(f.getFeature("http://xml.org/sax/features/namespaces"), "sax feature round-trip");
        boolean ok = false;
        try { f.getFeature("http://example.invalid/nope"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { ok = true; }
        check(ok, "sax getFeature(unknown) -> SAXNotRecognizedException");
        check(javax.xml.parsers.SAXParserFactory.newNSInstance().isNamespaceAware(), "newNSInstance");
        check(javax.xml.parsers.SAXParserFactory.newDefaultNSInstance().isNamespaceAware(), "newDefaultNSInstance");
        check(javax.xml.parsers.SAXParserFactory.newDefaultInstance() != null, "newDefaultInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
