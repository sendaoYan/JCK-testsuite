/*
 * @test
 * @summary javax.xml.validation.SchemaFactory conformance: white-box structure + black-box behavior.
 * @run main SchemaFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SchemaFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.validation.SchemaFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getResourceResolver"), "method getResourceResolver");
        check(hasMethod(c, "isSchemaLanguageSupported"), "method isSchemaLanguageSupported");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newSchema"), "method newSchema");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setResourceResolver"), "method setResourceResolver");

        javax.xml.validation.SchemaFactory f = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        check(f.isSchemaLanguageSupported(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI), "isSchemaLanguageSupported");
        check(f.newSchema() != null, "newSchema");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.validation.SchemaFactory f = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        check(f != null, "newInstance(W3C)");
        check(f.isSchemaLanguageSupported(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI), "W3C schema supported");
        check(!f.isSchemaLanguageSupported("http://example.invalid/lang"), "unknown schema language unsupported");
        check(f.getErrorHandler() == null && f.getResourceResolver() == null, "default handlers null");
        String XSD = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='xs:string'/></xs:schema>";
        javax.xml.validation.Schema s = f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));
        check(s != null, "newSchema(Source)");
        check(f.newSchema() != null, "newSchema() empty");
        javax.xml.validation.Schema schemas = f.newSchema(new javax.xml.transform.Source[] {
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD))});
        check(schemas != null, "newSchema(Source[])");
        boolean ok = false;
        try { f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='bogus'/></xs:schema>"))); }
        catch (org.xml.sax.SAXException e) { ok = true; }
        check(ok, "invalid schema -> SAXException");
        check(javax.xml.validation.SchemaFactory.newDefaultInstance() != null, "newDefaultInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
