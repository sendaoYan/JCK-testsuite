/*
 * @test
 * @summary javax.xml.parsers.SAXParser conformance: parse overloads, ContentHandler callback order, namespace-aware callbacks, exceptions, reset.
 * @run main SAXParserTest
 */

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
