/*
 * @test
 * @summary javax.xml.validation.ValidatorHandler conformance: white-box structure + black-box behavior.
 * @run main ValidatorHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ValidatorHandlerTest {
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

        Class<?> c = Class.forName("javax.xml.validation.ValidatorHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasMethod(c, "getContentHandler"), "method getContentHandler");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getResourceResolver"), "method getResourceResolver");
        check(hasMethod(c, "getTypeInfoProvider"), "method getTypeInfoProvider");
        check(hasMethod(c, "setContentHandler"), "method setContentHandler");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setResourceResolver"), "method setResourceResolver");

        javax.xml.validation.ValidatorHandler vh = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema().newValidatorHandler();
        check(true, "newValidatorHandler");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='xs:string'/></xs:schema>")));
        javax.xml.validation.ValidatorHandler vh = s.newValidatorHandler();
        check(vh.getContentHandler() == null, "default content handler null");
        vh.setContentHandler(null); check(vh.getContentHandler() == null, "content handler round-trip");
        vh.setErrorHandler(null); check(vh.getErrorHandler() == null, "error handler round-trip");
        vh.setResourceResolver(null); check(vh.getResourceResolver() == null, "resource resolver round-trip");
        javax.xml.validation.TypeInfoProvider tip = vh.getTypeInfoProvider();
        check(tip != null, "getTypeInfoProvider");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
