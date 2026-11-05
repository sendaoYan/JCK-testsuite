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
        testPipeline();
        testTypeInfo();
        testConfig();
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
    static final String BAD = "<root id='7'><count>42</count></root>";

    interface Thrower { void run() throws Exception; }
    static Throwable ex(Thrower t) {
        try { t.run(); return null; }
        catch (Throwable e) { return e; }
    }

    static javax.xml.validation.Schema schema() throws Exception {
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));
    }

    static org.xml.sax.XMLReader reader() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        return spf.newSAXParser().getXMLReader();
    }

    // ---- SAX pipeline cascade: XMLReader -> ValidatorHandler -> recording handler ----
    static void testPipeline() throws Exception {
        javax.xml.validation.ValidatorHandler vh = schema().newValidatorHandler();
        final java.util.List<String> events = new java.util.ArrayList<String>();
        vh.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startDocument() { events.add("startDocument"); }
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs) {
                events.add("start:" + localName);
            }
            public void endElement(String uri, String localName, String qName) { events.add("end:" + localName); }
            public void endDocument() { events.add("endDocument"); }
        });
        org.xml.sax.XMLReader xr = reader();
        xr.setContentHandler(vh);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(GOOD)));
        check(events.equals(java.util.Arrays.asList(
                "startDocument", "start:root", "start:name", "end:name", "start:count", "end:count", "end:root", "endDocument")),
              "augmented event sequence exact: " + events);

        // second cascade with the same ValidatorHandler works again
        events.clear();
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(GOOD)));
        check(events.size() == 8, "ValidatorHandler reusable for second parse");
    }

    // ---- TypeInfoProvider: PSVI type info visible inside startElement callbacks ----
    static void testTypeInfo() throws Exception {
        javax.xml.validation.ValidatorHandler vh = schema().newValidatorHandler();
        javax.xml.validation.TypeInfoProvider tip = vh.getTypeInfoProvider();
        check(tip != null, "getTypeInfoProvider non-null");
        final javax.xml.validation.TypeInfoProvider captured = tip;
        final String[] elemTypeName = new String[3];
        final String[] elemTypeNs = new String[3];
        final String[] attrTypeName = new String[1];
        final boolean[] attrSpecified = new boolean[1];
        final boolean[] attrIsId = new boolean[1];
        vh.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            int idx = 0;
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs) {
                org.w3c.dom.TypeInfo ti = captured.getElementTypeInfo();
                elemTypeName[idx] = ti.getTypeName();
                elemTypeNs[idx] = ti.getTypeNamespace();
                if ("root".equals(localName) && attrs.getLength() > 0) {
                    org.w3c.dom.TypeInfo ai = captured.getAttributeTypeInfo(0);
                    attrTypeName[0] = ai == null ? null : ai.getTypeName();
                    attrSpecified[0] = captured.isSpecified(0);
                    attrIsId[0] = captured.isIdAttribute(0);
                }
                idx++;
            }
        });
        org.xml.sax.XMLReader xr = reader();
        xr.setContentHandler(vh);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(GOOD)));
        // root has anonymous complex type; name is xs:string; count is xs:int
        check(elemTypeName[0] != null, "root element type name non-null");
        check(elemTypeName[1] != null && elemTypeName[1].equals("string"),
              "name element type is xs:string (got " + elemTypeName[1] + ")");
        check("http://www.w3.org/2001/XMLSchema".equals(elemTypeNs[1]), "name element type namespace is XSD ns");
        check(elemTypeName[2] != null && elemTypeName[2].equals("int"),
              "count element type is xs:int (got " + elemTypeName[2] + ")");
        check("http://www.w3.org/2001/XMLSchema".equals(elemTypeNs[2]), "count element type namespace is XSD ns");
        check("int".equals(attrTypeName[0]), "id attribute type is xs:int");
        check(attrSpecified[0], "id attribute isSpecified(0) true");
        check(!attrIsId[0], "id attribute isIdAttribute(0) false (xs:int is no ID type)");
    }

    // ---- error reporting through the pipeline + config matrix ----
    static void testConfig() throws Exception {
        // schema violation surfaces through the ValidatorHandler error handler, parse continues
        javax.xml.validation.ValidatorHandler vh = schema().newValidatorHandler();
        final java.util.List<String> errs = new java.util.ArrayList<String>();
        vh.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void error(org.xml.sax.SAXParseException e) { errs.add(e.getMessage()); }
        });
        vh.setContentHandler(new org.xml.sax.helpers.DefaultHandler());
        org.xml.sax.XMLReader xr = reader();
        xr.setContentHandler(vh);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(BAD)));
        check(errs.size() == 1 && errs.get(0).contains("cvc-complex-type"),
              "pipeline error handler receives cvc- error exactly once");

        // features/properties
        javax.xml.validation.ValidatorHandler vh2 = schema().newValidatorHandler();
        String FSP = javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
        vh2.setFeature(FSP, true);
        check(vh2.getFeature(FSP), "setFeature(secure,true)/getFeature round-trip");
        check(ex(() -> vh2.setFeature("http://bogus/f", true)) instanceof org.xml.sax.SAXNotRecognizedException,
              "setFeature(unknown) -> SAXNotRecognizedException");
        String PROP = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
        vh2.setProperty(PROP, "loc.xsd");
        check("loc.xsd".equals(vh2.getProperty(PROP)), "setProperty/getProperty recognized round-trip");
        check(ex(() -> vh2.setProperty("http://bogus/p", "x")) instanceof org.xml.sax.SAXNotRecognizedException,
              "setProperty(unknown) -> SAXNotRecognizedException");
        check(ex(() -> vh2.getProperty("http://bogus/p")) instanceof org.xml.sax.SAXNotRecognizedException,
              "getProperty(unknown) -> SAXNotRecognizedException");

        // content/error handler null-clearing semantics
        vh2.setContentHandler(null);
        check(vh2.getContentHandler() == null, "setContentHandler(null) clears");
        vh2.setErrorHandler(null);
        check(vh2.getErrorHandler() == null, "setErrorHandler(null) clears");
    }
}

