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
        testContract();
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

    static final String EXTERNAL_NO_NS_PROP =
        "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    interface Thrower { void run() throws Exception; }

    static Throwable ex(Thrower t) {
        try { t.run(); return null; }
        catch (Throwable e) { return e; }
    }

    static void testContract() throws Exception {
        // ---- constants and language boundary ----
        check(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI.equals("http://www.w3.org/2001/XMLSchema"),
              "W3C_XML_SCHEMA_NS_URI exact value");
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        check(f != null, "newInstance(W3C constant) non-null");
        check(f.isSchemaLanguageSupported(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI), "isSchemaLanguageSupported(W3C)");
        Throwable t1 = ex(() -> javax.xml.validation.SchemaFactory.newInstance("http://bogus/ns"));
        check(t1 instanceof IllegalArgumentException, "newInstance(bogus) -> IllegalArgumentException");
        check(t1.getMessage() != null && t1.getMessage().contains("http://bogus/ns"), "IAE message names the language");
        check(ex(() -> javax.xml.validation.SchemaFactory.newInstance(null)) instanceof NullPointerException,
              "newInstance(null) -> NPE");
        check(ex(() -> f.isSchemaLanguageSupported(null)) instanceof NullPointerException,
              "isSchemaLanguageSupported(null) -> NPE");

        // ---- feature round-trips ----
        String FSP = javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
        f.setFeature(FSP, true);
        check(f.getFeature(FSP), "setFeature(secure,true)/getFeature round-trip");
        f.setFeature(FSP, false);
        check(!f.getFeature(FSP), "setFeature(secure,false)/getFeature round-trip");
        check(ex(() -> f.setFeature("http://bogus/feature", true)) instanceof org.xml.sax.SAXNotRecognizedException,
              "setFeature(unknown) -> SAXNotRecognizedException");
        check(ex(() -> f.getFeature("http://bogus/feature")) instanceof org.xml.sax.SAXNotRecognizedException,
              "getFeature(unknown) -> SAXNotRecognizedException");

        // ---- property round-trips ----
        check(ex(() -> f.setProperty("http://bogus/prop", "x")) instanceof org.xml.sax.SAXNotRecognizedException,
              "setProperty(unknown) -> SAXNotRecognizedException");
        check(ex(() -> f.getProperty("http://bogus/prop")) instanceof org.xml.sax.SAXNotRecognizedException,
              "getProperty(unknown) -> SAXNotRecognizedException");
        f.setProperty(EXTERNAL_NO_NS_PROP, "loc.xsd");
        check("loc.xsd".equals(f.getProperty(EXTERNAL_NO_NS_PROP)), "setProperty/getProperty(external-noNamespaceSchemaLocation)");

        // ---- error handler / resource resolver round-trips ----
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        f.setErrorHandler(eh);
        check(f.getErrorHandler() == eh, "setErrorHandler/getErrorHandler round-trip");
        f.setErrorHandler(null);
        check(f.getErrorHandler() == null, "setErrorHandler(null) clears");
        check(f.getResourceResolver() == null, "default LSResourceResolver null");
        org.w3c.dom.ls.LSResourceResolver rr = new org.w3c.dom.ls.LSResourceResolver() {
            public org.w3c.dom.ls.LSInput resolveResource(String type, String ns, String pid, String sid, String base) { return null; }
        };
        f.setResourceResolver(rr);
        check(f.getResourceResolver() == rr, "setResourceResolver/getResourceResolver round-trip");
        f.setResourceResolver(null);
        check(f.getResourceResolver() == null, "setResourceResolver(null) clears");

        // ---- newSchema(Source) closed loop ----
        javax.xml.transform.stream.StreamSource xsdSrc =
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD));
        javax.xml.validation.Schema s = f.newSchema(xsdSrc);
        check(s != null, "newSchema(Source) non-null");
        javax.xml.validation.Validator v = s.newValidator();
        v.validate(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<root id='7'><name>abc</name><count>42</count></root>")));
        check(true, "valid document passes newSchema(Source) validator");
        Throwable t2 = ex(() -> v.validate(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<root id='7'><count>42</count></root>"))));
        check(t2 instanceof org.xml.sax.SAXParseException, "invalid document -> SAXParseException");
        check(t2.getMessage() != null && t2.getMessage().startsWith("cvc-complex-type"),
              "invalid document message starts with cvc-complex-type");

        // ---- newSchema(Source[]) ----
        javax.xml.validation.Schema s2 = f.newSchema(new javax.xml.transform.Source[] {
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)),
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD))});
        check(s2 != null, "newSchema(Source[]) non-null");

        // ---- newSchema(File) with temp file (finally deleted) ----
        java.io.File tmp = java.io.File.createTempFile("sfctest", ".xsd");
        try {
            java.io.FileWriter w = new java.io.FileWriter(tmp);
            try { w.write(XSD); } finally { w.close(); }
            javax.xml.validation.Schema sf2 = f.newSchema(tmp);
            check(sf2 != null, "newSchema(File) non-null");
            javax.xml.validation.Schema su = f.newSchema(tmp.toURI().toURL());
            check(su != null, "newSchema(URL) non-null");
        } finally {
            check(tmp.delete(), "temp XSD deleted");
        }

        // ---- newSchema() empty schema: JDK-8 internal impl validates strictly ----
        javax.xml.validation.Schema empty = f.newSchema();
        javax.xml.validation.Validator ev = empty.newValidator();
        Throwable t3 = ex(() -> ev.validate(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<anything><goes/></anything>"))));
        check(t3 instanceof org.xml.sax.SAXParseException, "newSchema() rejects undeclared element (JDK-8: cvc-elt.1)");
        check(t3.getMessage() != null && t3.getMessage().startsWith("cvc-elt.1"),
              "empty-schema message starts with cvc-elt.1");

        // ---- invalid XSD ----
        Throwable t4 = ex(() -> f.newSchema(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element></xs:schema>"))));
        check(t4 instanceof org.xml.sax.SAXException, "syntax-broken XSD -> SAXException");
        check(t4.getMessage() != null && !t4.getMessage().isEmpty(), "syntax-broken XSD message non-empty");
        Throwable t5 = ex(() -> f.newSchema(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader("<schema/>"))));
        check(t5 instanceof org.xml.sax.SAXException && t5.getMessage().contains("s4s-elt-schema-ns"),
              "wrong-namespace XSD -> SAXException containing s4s-elt-schema-ns");

        // ---- newInstance(uri, className, loader) ----
        javax.xml.validation.SchemaFactory f3 = javax.xml.validation.SchemaFactory.newInstance(
            javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI,
            "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
            Thread.currentThread().getContextClassLoader());
        check(f3 != null && f3.isSchemaLanguageSupported(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI),
              "newInstance(uri, className, loader) works");
    }


}
