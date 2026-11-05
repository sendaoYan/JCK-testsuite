/*
 * @test
 * @summary javax.xml.validation.Schema conformance: white-box structure + black-box behavior.
 * @run main SchemaTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SchemaTest {
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

        Class<?> c = Class.forName("javax.xml.validation.Schema");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "newValidator"), "method newValidator");
        check(hasMethod(c, "newValidatorHandler"), "method newValidatorHandler");

        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema();
        check(s.newValidator() != null, "newValidator");
        check(s.newValidatorHandler() != null, "newValidatorHandler");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.validation.SchemaFactory f = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.Schema s = f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='xs:string'/></xs:schema>")));
        check(s != null, "schema compiled");
        javax.xml.validation.Validator v = s.newValidator();
        check(v != null, "newValidator");
        javax.xml.validation.ValidatorHandler vh = s.newValidatorHandler();
        check(vh != null, "newValidatorHandler");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }


    static final String XSD =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
      + "<xs:element name='root'>"
      + "<xs:complexType>"
      + "<xs:sequence>"
      + "<xs:element name='name' type='xs:string' minOccurs='1'/>"
      + "<xs:element name='count' type='xs:int' minOccurs='1'/>"
      + "</xs:sequence>"
      + "<xs:attribute name='id' type='xs:int' use='required'/>"
      + "</xs:complexType>"
      + "</xs:element>"
      + "</xs:schema>";

    static final String BAD_DOC = "<root id='7'><count>42</count></root>";

    static void testBehavior() throws Exception {
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.Schema s =
            f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));

        // ---- newValidator: multiple distinct independent instances ----
        javax.xml.validation.Validator v1 = s.newValidator();
        javax.xml.validation.Validator v2 = s.newValidator();
        check(v1 != null && v2 != null, "newValidator non-null");
        check(v1 != v2, "newValidator returns fresh instance each call");

        // v1 gets a recording handler -> invalid doc reported via callback, validate returns
        final int[] errCalls = {0};
        v1.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void error(org.xml.sax.SAXParseException e) { errCalls[0]++; }
        });
        v1.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(BAD_DOC)));
        check(true, "v1 with error handler: validate returns normally");
        check(errCalls[0] == 1, "v1 error handler invoked exactly once");

        // v2 has no handler -> same document throws (handler state not shared)
        boolean threw = false;
        try {
            v2.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(BAD_DOC)));
        } catch (org.xml.sax.SAXParseException e) { threw = true; }
        check(threw, "v2 (separate instance) still throws: newValidator instances independent");
        check(v2.getErrorHandler() == null, "v2 error handler untouched by v1");

        // ---- newValidatorHandler: distinct, independent of validators ----
        javax.xml.validation.ValidatorHandler vh1 = s.newValidatorHandler();
        javax.xml.validation.ValidatorHandler vh2 = s.newValidatorHandler();
        check(vh1 != null && vh2 != null, "newValidatorHandler non-null");
        check(vh1 != vh2, "newValidatorHandler returns fresh instance each call");
        org.xml.sax.ContentHandler ch = new org.xml.sax.helpers.DefaultHandler();
        vh1.setContentHandler(ch);
        check(vh1.getContentHandler() == ch, "vh1 content handler round-trip");
        check(vh2.getContentHandler() == null, "vh2 content handler untouched by vh1");
    }
}

