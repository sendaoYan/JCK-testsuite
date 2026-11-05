/*
 * @test
 * @summary javax.xml.parsers.SAXParser conformance: parse overloads, ContentHandler callback order, namespace-aware callbacks, exceptions, reset.
 * @run main SAXParserTest
 */

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

public class SAXParserTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<root xmlns:p=\"urn:parts\"><p:item id=\"i1\">hello</p:item></root>";

    // records the sequence of SAX events as a compact string
    static class Recorder extends DefaultHandler {
        final StringBuilder sb = new StringBuilder();
        public void startDocument() throws SAXException { sb.append("[startDocument]"); }
        public void endDocument() throws SAXException { sb.append("[endDocument]"); }
        public void startPrefixMapping(String p, String u) throws SAXException { sb.append("[spm:").append(p).append("=").append(u).append("]"); }
        public void endPrefixMapping(String p) throws SAXException { sb.append("[epm:").append(p).append("]"); }
        public void startElement(String uri, String local, String qName, Attributes a) throws SAXException {
            sb.append("[start:").append(uri).append(',').append(local).append(',').append(qName);
            for (int i = 0; i < a.getLength(); i++) {
                sb.append(",@").append(a.getQName(i)).append('=').append(a.getValue(i));
            }
            sb.append("]");
        }
        public void endElement(String uri, String local, String qName) throws SAXException {
            sb.append("[end:").append(local).append("]");
        }
        public void characters(char[] ch, int s, int l) throws SAXException {
            sb.append("[text:").append(new String(ch, s, l)).append("]");
        }
        public void processingInstruction(String target, String data) throws SAXException {
            sb.append("[pi:").append(target).append("]");
        }
    }

    public static void main(String[] args) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        SAXParser p = f.newSAXParser();

        check(p.isNamespaceAware(), "namespace-aware parser");
        check(!p.isValidating(), "validating false");
        check(!p.isXIncludeAware(), "xIncludeAware false");
        check(p.getSchema() == null, "schema null");
        check(p.getXMLReader() != null, "getXMLReader non-null");
        check(p.getParser() != null, "getParser non-null");

        // ---- callback order over namespace-aware document ----
        Recorder r = new Recorder();
        p.parse(new InputSource(new ByteArrayInputStream(XML.getBytes("UTF-8"))), r);
        String ev = r.sb.toString();
        check(ev.startsWith("[startDocument]"), "startDocument first");
        check(ev.endsWith("[endDocument]"), "endDocument last");
        check(ev.contains("[start:urn:parts,item,p:item,@id=i1]"),
              "startElement with ns uri, local name, qName, attr");
        check(ev.contains("[text:hello]"), "characters content");
        check(ev.contains("[end:item]"), "endElement");
        // prefix mapping appears before the element that uses it
        check(ev.indexOf("[spm:p=urn:parts]") < ev.indexOf("[start:urn:parts,item"), "startPrefixMapping before element");

        // ---- parse(InputStream, DefaultHandler) ----
        Recorder r2 = new Recorder();
        p.parse(new ByteArrayInputStream(XML.getBytes("UTF-8")), r2);
        check(r2.sb.toString().contains("[start:urn:parts,item"), "parse(InputStream, handler)");

        // ---- parse(String uri) and parse(File) ----
        java.io.File tmp = java.io.File.createTempFile("saxparser", ".xml");
        tmp.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
        fos.write(XML.getBytes("UTF-8"));
        fos.close();
        Recorder r3 = new Recorder();
        p.parse(tmp.toURI().toString(), r3);
        check(r3.sb.toString().contains("[start:urn:parts,item"), "parse(String uri, handler)");
        Recorder r4 = new Recorder();
        p.parse(tmp, r4);
        check(r4.sb.toString().contains("[start:urn:parts,item"), "parse(File, handler)");
        tmp.delete();

        // ---- legacy HandlerBase overload ----
        final StringBuilder legacy = new StringBuilder();
        org.xml.sax.HandlerBase hb = new org.xml.sax.HandlerBase() {
            public void startElement(String name, org.xml.sax.AttributeList atts) { legacy.append("start:").append(name); }
        };
        p.parse(new InputSource(new ByteArrayInputStream(XML.getBytes("UTF-8"))), hb);
        check(legacy.toString().contains("item"), "parse(InputSource, HandlerBase)");

        // ---- null semantics ----
        check(isIae(() -> p.parse((InputStream) null, (DefaultHandler) null)),
              "parse(null InputStream, null handler) -> IAE");
        check(isIae(() -> p.parse((String) null, (DefaultHandler) null)),
              "parse(null uri, null handler) -> IAE");
        check(isIae(() -> p.parse((java.io.File) null, (DefaultHandler) null)),
              "parse(null File, null handler) -> IAE");
        check(isIae(() -> p.parse((InputSource) null, (DefaultHandler) null)),
              "parse(null InputSource, null handler) -> IAE");

        // ---- malformed XML -> SAXParseException with position ----
        boolean ok = false;
        try {
            p.parse(new InputSource(new ByteArrayInputStream("<a><b></a>".getBytes("UTF-8"))), new DefaultHandler());
        } catch (SAXParseException e) {
            ok = e.getLineNumber() > 0 && e.getColumnNumber() > 0;
        }
        check(ok, "malformed XML -> SAXParseException with position");

        // ---- setProperty/getProperty unknown -> SAXNotRecognizedException ----
        boolean notRecog = false;
        try { p.setProperty("http://no.such/property", "v"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { notRecog = true; }
        catch (SAXException e) { notRecog = true; }
        check(notRecog, "setProperty(unknown) -> SAXNotRecognizedException");

        // ---- reset keeps parser usable ----
        p.reset();
        Recorder r5 = new Recorder();
        p.parse(new InputSource(new ByteArrayInputStream(XML.getBytes("UTF-8"))), r5);
        check(r5.sb.toString().contains("[start:urn:parts,item"), "parser reusable after reset");

        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static InputSource in(String s) {
        return new InputSource(new StringReader(s));
    }

    static void testDeep() throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        SAXParser p = f.newSAXParser();

        // ---- full callback sequence locked as one string (ns-aware) ----
        Recorder full = new Recorder();
        p.parse(in(XML), full);
        check(full.sb.toString().equals(
                "[startDocument][spm:p=urn:parts][start:,root,root]"
                + "[start:urn:parts,item,p:item,@id=i1][text:hello][end:item][end:root][epm:p][endDocument]"),
              "complete ns-aware SAX event sequence: " + full.sb);

        // ---- setDocumentLocator first, PI callbacks precise ----
        final StringBuilder loc = new StringBuilder();
        p.parse(in("<?xml version=\"1.0\"?><?pi1 d1?><r><?pi2 d2?></r>"), new DefaultHandler() {
            public void setDocumentLocator(Locator l) { loc.append("loc:").append(l.getLineNumber() >= 0).append(';'); }
            public void processingInstruction(String t, String d) { loc.append("pi:").append(t).append(',').append(d).append(';'); }
        });
        String locS = loc.toString();
        check(locS.startsWith("loc:true;"), "setDocumentLocator delivered first, line >= 0");
        check(locS.contains("pi:pi1,d1;") && locS.contains("pi:pi2,d2;"), "PI target+data precise");

        // ---- characters chunking: only concatenation is guaranteed ----
        final StringBuilder chars = new StringBuilder();
        p.parse(in("<r>abcdef<!--c-->gh&amp;i</r>"), new DefaultHandler() {
            public void characters(char[] ch, int s, int l) { chars.append(new String(ch, s, l)); }
        });
        check(chars.toString().equals("abcdefgh&i"), "concatenated characters with entity decoding");

        // ---- ignorableWhitespace requires validating + DTD element content ----
        SAXParserFactory vf = SAXParserFactory.newInstance();
        vf.setValidating(true);
        vf.setNamespaceAware(true);
        SAXParser vp = vf.newSAXParser();
        final StringBuilder iw = new StringBuilder();
        final StringBuilder cv = new StringBuilder();
        vp.parse(in("<!DOCTYPE r [<!ELEMENT r (a,b)><!ELEMENT a (#PCDATA)><!ELEMENT b (#PCDATA)>]>"
                + "<r> <a>1</a> <b>2</b> </r>"), new DefaultHandler() {
            public void ignorableWhitespace(char[] ch, int s, int l) { iw.append('W'); }
            public void characters(char[] ch, int s, int l) { cv.append(new String(ch, s, l)); }
        });
        check(iw.length() == 3, "ignorableWhitespace for element-content whitespace (3 slots)");
        check(cv.toString().equals("12"), "element text via characters");

        // ---- Attributes matrix: ns-aware ----
        final StringBuilder am = new StringBuilder();
        p.parse(in("<r xmlns:p='urn:p' a='1' p:b='2'/>"), new DefaultHandler() {
            public void startElement(String u, String l, String q, Attributes at) {
                am.append("len=").append(at.getLength()).append(';');
                for (int i = 0; i < at.getLength(); i++)
                    am.append(i).append('[').append(at.getURI(i)).append('|').append(at.getLocalName(i))
                      .append('|').append(at.getQName(i)).append('|').append(at.getValue(i))
                      .append('|').append(at.getType(i)).append(']');
                am.append("byQ=").append(at.getValue("a")).append(',').append(at.getValue("p:b")).append(';');
                am.append("oob=").append(at.getValue(99)).append(';');
                am.append("miss=").append(at.getValue("zz")).append(';');
            }
        });
        String as = am.toString();
        check(as.startsWith("len=2;"), "ns-aware: xmlns not among attributes");
        check(as.contains("0[|a|a|1|CDATA]"), "ns-unqualified attribute fields");
        check(as.contains("1[urn:p|b|p:b|2|CDATA]"), "ns-qualified attribute fields (uri/local/qname/value/type)");
        check(as.contains("byQ=1,2;"), "getValue by qName");
        check(as.contains("oob=null;"), "out-of-range index -> null");
        check(as.contains("miss=null;"), "unknown qName -> null");

        // ---- Attributes matrix: ns-unaware (qName carries everything, xmlns visible) ----
        SAXParserFactory nf = SAXParserFactory.newInstance();
        SAXParser np = nf.newSAXParser();
        check(!np.isNamespaceAware(), "default factory not namespace aware");
        final StringBuilder am2 = new StringBuilder();
        np.parse(in("<r xmlns:p='urn:p' a='1' p:b='2'/>"), new DefaultHandler() {
            public void startElement(String u, String l, String q, Attributes at) {
                am2.append("hdr=").append(u).append(',').append(l).append(',').append(q).append(';');
                for (int i = 0; i < at.getLength(); i++)
                    am2.append(i).append('[').append(at.getURI(i)).append('|').append(at.getLocalName(i))
                       .append('|').append(at.getQName(i)).append(']');
            }
        });
        String as2 = am2.toString();
        check(as2.startsWith("hdr=,,r;"), "ns-unaware: empty uri/local, qName only");
        check(as2.contains("0[|xmlns:p|xmlns:p]") && as2.contains("1[|a|a]") && as2.contains("2[|p:b|p:b]"),
              "ns-unaware: xmlns declaration exposed as attribute: " + as2);
        check(as2.indexOf("1[|a|a]") > as2.indexOf("0[|xmlns:p|xmlns:p]"), "attribute order preserved (document order)");

        // ---- EntityResolver via DefaultHandler is adopted ----
        final boolean[] hitDH = { false };
        final StringBuilder entDH = new StringBuilder();
        p.parse(in("<!DOCTYPE r SYSTEM 'http://example.invalid/x.dtd'><r>&e;</r>"), new DefaultHandler() {
            public InputSource resolveEntity(String pub, String sys) {
                hitDH[0] = true;
                return new InputSource(new StringReader("<!ENTITY e 'SAX-RESOLVED'>"));
            }
            public void characters(char[] ch, int s, int l) { entDH.append(new String(ch, s, l)); }
        });
        check(hitDH[0], "DefaultHandler.resolveEntity consulted");
        check(entDH.toString().equals("SAX-RESOLVED"), "resolver-supplied entity text enters characters()");

        // ---- EntityResolver at XMLReader level (direct parse) ----
        SAXParser p2 = f.newSAXParser();
        XMLReader xr = p2.getXMLReader();
        check(xr == p2.getXMLReader(), "getXMLReader returns stable instance");
        check(xr.getFeature("http://xml.org/sax/features/namespaces"), "reader namespaces feature from factory");
        final boolean[] hitXR = { false };
        final StringBuilder entXR = new StringBuilder();
        xr.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String pub, String sys) {
                hitXR[0] = true;
                return new InputSource(new StringReader("<!ENTITY e 'XREADER-RESOLVED'>"));
            }
        });
        xr.setContentHandler(new DefaultHandler() {
            public void characters(char[] ch, int s, int l) { entXR.append(new String(ch, s, l)); }
        });
        xr.parse(in("<!DOCTYPE r SYSTEM 'http://example.invalid/x.dtd'><r>&e;</r>"));
        check(hitXR[0], "XMLReader.setEntityResolver consulted");
        check(entXR.toString().equals("XREADER-RESOLVED"), "reader-level resolver adoption");

        // ---- XMLReader feature toggling ----
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        check(xr.getFeature("http://xml.org/sax/features/namespace-prefixes"), "namespace-prefixes round-trip");

        // ---- lexical-handler property set/get ----
        Object lh = new org.xml.sax.ext.DefaultHandler2();
        p.setProperty("http://xml.org/sax/properties/lexical-handler", lh);
        Object got = p.getProperty("http://xml.org/sax/properties/lexical-handler");
        check(got != null, "lexical-handler property readable after set");

        // ---- precise unknown feature/property exceptions ----
        boolean snre = false;
        try { xr.setFeature("http://no.such/feature", true); }
        catch (org.xml.sax.SAXNotRecognizedException e) { snre = true; }
        check(snre, "XMLReader.setFeature(unknown) -> SAXNotRecognizedException");
        boolean snre2 = false;
        try { p.setProperty("http://no.such/prop", "v"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { snre2 = true; }
        check(snre2, "SAXParser.setProperty(unknown) -> SAXNotRecognizedException");
        boolean snre3 = false;
        try { p.getProperty("http://no.such/prop"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { snre3 = true; }
        check(snre3, "SAXParser.getProperty(unknown) -> SAXNotRecognizedException");

        // ---- validating factory without ErrorHandler: well-formed still parses ----
        SAXParserFactory vf2 = SAXParserFactory.newInstance();
        vf2.setValidating(true);
        SAXParser vp2 = vf2.newSAXParser();
        check(vp2.isValidating(), "validating factory -> validating parser");
        vp2.parse(in("<!DOCTYPE r [<!ELEMENT r (#PCDATA)>]><r>ok</r>"), new DefaultHandler());
        check(true, "validating parser handles valid doc without handler");
        boolean malformedStillFatal = false;
        try { vp2.parse(in("<a><b></a>"), new DefaultHandler()); }
        catch (SAXParseException e) { malformedStillFatal = e.getLineNumber() == 1; }
        check(malformedStillFatal, "well-formedness errors stay fatal under validating parser");

        // ---- reset restores configuration ----
        p.reset();
        Recorder afterReset = new Recorder();
        p.parse(in(XML), afterReset);
        check(afterReset.sb.toString().equals(full.sb.toString()), "identical event sequence after reset");

        // ---- prefix mapping lifecycle for nested default ns ----
        final StringBuilder pmap = new StringBuilder();
        p.parse(in("<r xmlns='urn:d'><c/></r>"), new DefaultHandler() {
            public void startPrefixMapping(String p, String u) { pmap.append('+').append(p).append('=').append(u).append(';'); }
            public void endPrefixMapping(String p) { pmap.append('-').append(p).append(';'); }
            public void startElement(String u, String l, String q, Attributes a) { pmap.append('<').append(u).append('#').append(l).append(';'); }
            public void endElement(String u, String l, String q) { pmap.append('>').append(';'); }
        });
        check(pmap.toString().equals("+=urn:d;<urn:d#r;<urn:d#c;>;>;-;"),
              "default ns prefix mapping lifecycle: " + pmap);
    }
}
