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
        testCvcMessages();
        testErrorHandlerMatrix();
        testResultForms();
        testResetAndConfig();
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

    static final String GOOD = "<root id='7'><name>abc</name><count>42</count></root>";
    static final String MISSING_CHILD = "<root id='7'><count>42</count></root>";
    static final String NON_INT = "<root id='7'><name>abc</name><count>zz</count></root>";
    static final String UNKNOWN_ELEM = "<root id='7'><name>abc</name><count>42</count><extra>x</extra></root>";
    static final String MISSING_ATTR = "<root><name>abc</name><count>42</count></root>";
    static final String WRONG_ORDER = "<root id='7'><count>42</count><name>abc</name></root>";

    interface Thrower { void run() throws Exception; }
    static Throwable ex(Thrower t) {
        try { t.run(); return null; }
        catch (Throwable e) { return e; }
    }

    static javax.xml.validation.Validator validator() throws Exception {
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD))).newValidator();
    }

    static javax.xml.transform.stream.StreamSource src(String xml) {
        return new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml));
    }

    // ---- XSD validation closed loop: cvc- message-level assertions ----
    static void testCvcMessages() throws Exception {
        javax.xml.validation.Validator v = validator();
        v.validate(src(GOOD));
        check(true, "valid document validates without exception");

        Throwable t = ex(() -> v.validate(src(MISSING_CHILD)));
        check(t instanceof org.xml.sax.SAXParseException, "missing required child -> SAXParseException");
        check(t.getMessage() != null && t.getMessage().contains("cvc-complex-type.2.4.a"),
              "missing child message contains cvc-complex-type.2.4.a");
        check(t.getMessage().contains("{name}"), "missing child message names expected element '{name}'");
        org.xml.sax.SAXParseException spe = (org.xml.sax.SAXParseException) t;
        check(spe.getLineNumber() >= 1 && spe.getColumnNumber() >= 1, "SAXParseException carries line/column >= 1");

        Throwable t2 = ex(() -> v.validate(src(NON_INT)));
        check(t2 instanceof org.xml.sax.SAXParseException, "non-int content -> SAXParseException");
        check(t2.getMessage().contains("cvc-datatype-valid.1.2.1"),
              "non-int message contains cvc-datatype-valid.1.2.1");
        check(t2.getMessage().contains("'zz'"), "non-int message quotes offending value");

        Throwable t3 = ex(() -> v.validate(src(UNKNOWN_ELEM)));
        check(t3 instanceof org.xml.sax.SAXParseException, "unknown element -> SAXParseException");
        check(t3.getMessage().contains("cvc-complex-type.2.4.d"),
              "unknown element message contains cvc-complex-type.2.4.d");

        Throwable t4 = ex(() -> v.validate(src(MISSING_ATTR)));
        check(t4 instanceof org.xml.sax.SAXParseException, "missing required attribute -> SAXParseException");
        check(t4.getMessage().contains("cvc-complex-type.4"),
              "missing attribute message contains cvc-complex-type.4");

        Throwable t5 = ex(() -> v.validate(src(WRONG_ORDER)));
        check(t5 instanceof org.xml.sax.SAXParseException, "wrong sequence order -> SAXParseException");
        check(t5.getMessage().contains("cvc-complex-type"),
              "wrong order message contains cvc-complex-type");

        // well-formedness error is fatal regardless of schema
        Throwable t6 = ex(() -> v.validate(src("<root id='1'><name>x</name>")));
        check(t6 instanceof org.xml.sax.SAXParseException, "truncated document -> SAXParseException");
    }

    // ---- ErrorHandler callback matrix vs no-handler behavior ----
    static void testErrorHandlerMatrix() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.validation.Validator v = validator();
        v.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void warning(org.xml.sax.SAXParseException e) { calls.add("warning"); }
            public void error(org.xml.sax.SAXParseException e) { calls.add("error:" + e.getMessage()); }
            public void fatalError(org.xml.sax.SAXParseException e) { calls.add("fatal"); }
        });
        // error handler consumes the error: validate must NOT throw
        v.validate(src(MISSING_CHILD));
        check(calls.size() == 1 && calls.get(0).startsWith("error:"),
              "handler receives exactly one error callback (no throw)");
        check(calls.get(0).contains("cvc-complex-type"), "callback message carries cvc- code");

        // same document without handler throws directly
        javax.xml.validation.Validator plain = validator();
        Throwable t = ex(() -> plain.validate(src(MISSING_CHILD)));
        check(t instanceof org.xml.sax.SAXParseException, "no handler: same document throws SAXParseException");

        // handler state round-trip
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        javax.xml.validation.Validator v2 = validator();
        v2.setErrorHandler(eh);
        check(v2.getErrorHandler() == eh, "setErrorHandler/getErrorHandler round-trip");
        v2.setErrorHandler(null);
        check(v2.getErrorHandler() == null, "setErrorHandler(null) clears");

        // LSResourceResolver round-trip
        org.w3c.dom.ls.LSResourceResolver rr = new org.w3c.dom.ls.LSResourceResolver() {
            public org.w3c.dom.ls.LSInput resolveResource(String type, String ns, String pid, String sid, String b) { return null; }
        };
        v2.setResourceResolver(rr);
        check(v2.getResourceResolver() == rr, "setResourceResolver/getResourceResolver round-trip");
    }

    // ---- validate(Source, Result) forms ----
    static void testResultForms() throws Exception {
        javax.xml.validation.Validator v = validator();
        // null Result = validation only
        v.validate(src(GOOD), null);
        check(true, "validate(Source, null Result) validation-only passes");

        // StreamResult receives the augmented output
        java.io.StringWriter sw = new java.io.StringWriter();
        v.validate(src(GOOD), new javax.xml.transform.stream.StreamResult(sw));
        String out = sw.toString();
        check(out.contains("<root") && out.contains("<count>42</count>"), "StreamResult receives serialized output");

        // DOMSource and SAXSource input forms
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(
            new org.xml.sax.InputSource(new java.io.StringReader(GOOD)));
        v.validate(new javax.xml.transform.dom.DOMSource(doc));
        check(true, "validate(DOMSource) passes");
        v.validate(new javax.xml.transform.sax.SAXSource(
            new org.xml.sax.InputSource(new java.io.StringReader(GOOD))));
        check(true, "validate(SAXSource) passes");

        // validator reusable across multiple validate calls
        v.validate(src(GOOD));
        v.validate(src(GOOD));
        check(true, "validator reusable across calls");
    }

    // ---- reset + feature/property matrix ----
    static void testResetAndConfig() throws Exception {
        javax.xml.validation.Validator v = validator();
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        v.setErrorHandler(eh);
        v.reset();
        check(v.getErrorHandler() == null, "reset clears error handler");
        v.validate(src(GOOD));
        check(true, "validator usable after reset");

        String FSP = javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
        v.setFeature(FSP, true);
        check(v.getFeature(FSP), "setFeature(secure,true)/getFeature round-trip");
        v.setFeature(FSP, false);
        check(!v.getFeature(FSP), "setFeature(secure,false)/getFeature round-trip");
        check(ex(() -> v.setFeature("http://bogus/f", true)) instanceof org.xml.sax.SAXNotRecognizedException,
              "setFeature(unknown) -> SAXNotRecognizedException");
        check(ex(() -> v.getFeature("http://bogus/f")) instanceof org.xml.sax.SAXNotRecognizedException,
              "getFeature(unknown) -> SAXNotRecognizedException");
        String PROP = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
        v.setProperty(PROP, "loc.xsd");
        check("loc.xsd".equals(v.getProperty(PROP)), "setProperty/getProperty recognized round-trip");
        check(ex(() -> v.setProperty("http://bogus/p", "x")) instanceof org.xml.sax.SAXNotRecognizedException,
              "setProperty(unknown) -> SAXNotRecognizedException");
        check(ex(() -> v.getProperty("http://bogus/p")) instanceof org.xml.sax.SAXNotRecognizedException,
              "getProperty(unknown) -> SAXNotRecognizedException");
    }
}

