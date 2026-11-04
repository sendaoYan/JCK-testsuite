/*
 * @test
 * @summary javax.xml.validation.Validator conformance: white-box structure + black-box behavior.
 * @run main ValidatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ValidatorTest {
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

        Class<?> c = Class.forName("javax.xml.validation.Validator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getResourceResolver"), "method getResourceResolver");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setResourceResolver"), "method setResourceResolver");
        check(hasMethod(c, "validate"), "method validate");

        javax.xml.validation.Validator v = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema().newValidator();
        v.reset();
        check(true, "reset");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        String XSD = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='xs:integer'/></xs:schema>";
        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));
        javax.xml.validation.Validator v = s.newValidator();
        // valid document passes
        v.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<n>123</n>")));
        check(true, "valid document validates");
        // invalid document -> SAXParseException (subclass of SAXException)
        boolean ok = false;
        try { v.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<n>abc</n>"))); }
        catch (org.xml.sax.SAXException e) { ok = true; }
        check(ok, "invalid document -> SAXException");
        // error handler round-trip
        v.setErrorHandler(null); check(v.getErrorHandler() == null, "error handler null");
        v.setResourceResolver(null); check(v.getResourceResolver() == null, "resource resolver null");
        v.reset();
        v.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<n>123</n>")));
        check(true, "validator reusable after reset");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
