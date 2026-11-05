/*
 * @test
 * @summary javax.xml.parsers.SAXParserFactory conformance: factory boundary, precise exceptions, null semantics, feature round-trip.
 * @run main SAXParserFactoryTest
 */

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;

public class SAXParserFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (FactoryConfigurationError e) { return true; }
        catch (Exception e) { return false; }
    }
    // any declared/undocumented exception from setFeature/getFeature
    static boolean throwsException(Thrower t) {
        try { t.run(); return false; }
        catch (Exception e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        check(f != null, "newInstance non-null");

        ClassLoader cl = SAXParserFactory.class.getClassLoader();
        SAXParserFactory f2 = SAXParserFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        check(isFce(() -> SAXParserFactory.newInstance("no.such.Factory", cl)),
              "bogus impl class -> FactoryConfigurationError");
        check(isFce(() -> SAXParserFactory.newInstance(null, null)),
              "null factory class -> FactoryConfigurationError");

        // documented defaults
        check(!f.isNamespaceAware(), "default namespaceAware=false");
        check(!f.isValidating(), "default validating=false");
        check(!f.isXIncludeAware(), "default xIncludeAware=false");
        check(f.getSchema() == null, "default schema=null");

        // round-trip
        f.setNamespaceAware(true);  check(f.isNamespaceAware(), "namespaceAware=true");
        f.setNamespaceAware(false); check(!f.isNamespaceAware(), "namespaceAware=false");
        f.setValidating(true);      check(f.isValidating(), "validating=true");
        f.setValidating(false);     check(!f.isValidating(), "validating=false");
        f.setXIncludeAware(true);   check(f.isXIncludeAware(), "xIncludeAware=true");
        f.setXIncludeAware(false);  check(!f.isXIncludeAware(), "xIncludeAware=false");
        f.setSchema(null);          check(f.getSchema() == null, "setSchema(null)");

        // unknown feature -> exception
        check(throwsException(() -> f.setFeature("http://no.such/feature", true)), "setFeature(unknown) throws");
        check(throwsException(() -> f.getFeature("http://no.such/feature")), "getFeature(unknown) throws");

        // parser production
        SAXParser p = f.newSAXParser();
        check(p != null, "newSAXParser non-null");
        check(p.isNamespaceAware() == f.isNamespaceAware(), "parser mirrors factory namespaceAware");
        check(p.isValidating() == f.isValidating(), "parser mirrors factory validating");
        SAXParser p2 = f.newSAXParser();
        check(p2 != null && p2 != p, "newSAXParser returns fresh parser");

        // ---- JDK-19+ factory methods ----
        check(SAXParserFactory.newDefaultInstance() != null, "newDefaultInstance non-null");
        check(SAXParserFactory.newNSInstance() != null, "newNSInstance non-null");
        check(SAXParserFactory.newDefaultNSInstance() != null, "newDefaultNSInstance non-null");

        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static boolean isSnre(Thrower t) {
        try { t.run(); return false; }
        catch (SAXNotRecognizedException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }

    static void testDeep() throws Exception {
        // ---- precise exceptions for unknown feature names ----
        SAXParserFactory f = SAXParserFactory.newInstance();
        check(isSnre(() -> f.setFeature("http://no.such/feature", true)),
              "setFeature(unknown) -> SAXNotRecognizedException");
        check(isSnre(() -> f.getFeature("http://no.such/feature")),
              "getFeature(unknown) -> SAXNotRecognizedException");
        check(isNpe(() -> f.getFeature(null)), "getFeature(null) -> NullPointerException");
        check(isNpe(() -> f.setFeature(null, true)), "setFeature(null,..) -> NullPointerException");

        // ---- known SAX feature round-trips ----
        f.setFeature("http://xml.org/sax/features/namespaces", true);
        check(f.getFeature("http://xml.org/sax/features/namespaces"), "namespaces feature true round-trip");
        f.setFeature("http://xml.org/sax/features/namespaces", false);
        check(!f.getFeature("http://xml.org/sax/features/namespaces"), "namespaces feature false round-trip");
        f.setFeature("http://xml.org/sax/features/validation", true);
        check(f.getFeature("http://xml.org/sax/features/validation"), "validation feature round-trip");

        // ---- factory flag and feature are independent knobs ----
        SAXParserFactory g = SAXParserFactory.newInstance();
        check(!g.isNamespaceAware(), "default namespaceAware=false");
        g.setNamespaceAware(true);
        SAXParser p = g.newSAXParser();
        check(p.isNamespaceAware(), "parser mirrors setNamespaceAware(true)");
        XMLReader xr = p.getXMLReader();
        check(xr.getFeature("http://xml.org/sax/features/namespaces"), "reader namespaces feature true after flag");
        SAXParserFactory g2 = SAXParserFactory.newInstance();
        SAXParser p2 = g2.newSAXParser();
        check(!p2.getXMLReader().getFeature("http://xml.org/sax/features/namespaces"),
              "reader namespaces feature false by default");

        // ---- reader feature toggles observable in callbacks ----
        final StringBuilder nsEv = new StringBuilder();
        p.getXMLReader().setContentHandler(new DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) {
                nsEv.append(u).append(',').append(l).append(',').append(q).append(';');
            }
        });
        p.getXMLReader().parse(new org.xml.sax.InputSource(new StringReader("<r xmlns:x='urn:x'><x:c/></r>")));
        check(nsEv.toString().equals(",r,r;urn:x,c,x:c;"), "ns-aware reader splits uri/local/qname: " + nsEv);

        // ---- schema identity round-trip ----
        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new javax.xml.transform.stream.StreamSource(
                        new StringReader("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
                                + "<xs:element name='r'/></xs:schema>")));
        SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setSchema(s);
        check(sf.getSchema() == s, "setSchema/getSchema identity");
        SAXParser sp = sf.newSAXParser();
        check(sp.getSchema() == s, "parser carries schema instance");
        sf.setSchema(null);
        check(sf.getSchema() == null, "setSchema(null) clears");

        // ---- namespaceAware parser validates qname form in callback ----
        SAXParserFactory h = SAXParserFactory.newInstance();
        h.setValidating(true);
        SAXParser hp = h.newSAXParser();
        check(hp.isValidating(), "validating parser");
        hp.parse(new org.xml.sax.InputSource(new StringReader(
                "<!DOCTYPE r [<!ELEMENT r (#PCDATA)>]><r>ok</r>")), new DefaultHandler());
        check(true, "validating parse completes with DTD");
    }
}
