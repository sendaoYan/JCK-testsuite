/*
 * @test
 * @summary javax.xml.stream.XMLStreamWriter conformance: interface hierarchy and methods.
 * @run main XMLStreamWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamWriter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("writeStartElement"), "has method writeStartElement");
        check(names.contains("writeEmptyElement"), "has method writeEmptyElement");
        check(names.contains("writeEndElement"), "has method writeEndElement");
        check(names.contains("writeEndDocument"), "has method writeEndDocument");
        check(names.contains("close"), "has method close");
        check(names.contains("flush"), "has method flush");
        check(names.contains("writeAttribute"), "has method writeAttribute");
        check(names.contains("writeNamespace"), "has method writeNamespace");
        check(names.contains("writeDefaultNamespace"), "has method writeDefaultNamespace");
        check(names.contains("writeComment"), "has method writeComment");
        check(names.contains("writeProcessingInstruction"), "has method writeProcessingInstruction");
        check(names.contains("writeCData"), "has method writeCData");
        check(names.contains("writeDTD"), "has method writeDTD");
        check(names.contains("writeEntityRef"), "has method writeEntityRef");
        check(names.contains("writeStartDocument"), "has method writeStartDocument");
        check(names.contains("writeCharacters"), "has method writeCharacters");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("setPrefix"), "has method setPrefix");
        check(names.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(names.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("getProperty"), "has method getProperty");
        testWriteReadRoundTrip();
        testWriterForms();
        testWriterEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: write -> parse-back round trip (core closure) ----
    static void testWriteReadRoundTrip() throws Exception {
        javax.xml.stream.XMLOutputFactory of = javax.xml.stream.XMLOutputFactory.newFactory();
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = of.createXMLStreamWriter(sw);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeComment("c");
        w.writeProcessingInstruction("pi", "data");
        w.writeStartElement("root");
        w.writeNamespace("p", "urn:p");
        w.writeDefaultNamespace("urn:d");
        w.writeAttribute("p", "urn:p", "k", "v1");
        w.writeAttribute("plain", "v2");
        w.writeCharacters("a<b>&c\"d'");
        w.writeCData("raw <c>");
        w.writeCharacters("z");
        w.writeEmptyElement("p", "e", "urn:p");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        String out = sw.toString();
        String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--c--><?pi data?>"
            + "<root xmlns:p=\"urn:p\" xmlns=\"urn:d\" p:k=\"v1\" plain=\"v2\">"
            + "a&lt;b&gt;&amp;c\"d'<![CDATA[raw <c>]]>z<p:e/></root>";
        check(expect.equals(out), "writer output exact (escaping + ns decls + CDATA + empty element), got [" + out + "]");
        // parse back and assert tree/text/attribute equality
        javax.xml.stream.XMLInputFactory inf = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = inf.createXMLStreamReader(new java.io.StringReader(out));
        r.next(); // comment (written before the PI)
        check(r.getEventType() == 5 && "c".equals(r.getText()), "read back comment");
        r.next(); // PI
        check(r.getEventType() == 3 && "pi".equals(r.getPITarget()) && "data".equals(r.getPIData()), "read back PI");
        r.next(); // START root
        check(r.getName().equals(new javax.xml.namespace.QName("urn:d", "root")), "read back root QName");
        check(r.getAttributeCount() == 2 && "v1".equals(r.getAttributeValue(0)) && "v2".equals(r.getAttributeValue(1)),
            "read back attributes");
        check(r.getAttributeName(0).equals(new javax.xml.namespace.QName("urn:p", "k", "p")), "read back attr QName");
        StringBuilder text = new StringBuilder();
        int ev;
        while ((ev = r.next()) != 1) text.append(r.getText()); // gather until <p:e>
        check("a<b>&c\"d'raw <c>z".equals(text.toString()), "read back concatenated text incl CDATA, got [" + text + "]");
        check("e".equals(r.getLocalName()) && "urn:p".equals(r.getNamespaceURI()), "read back empty element START");
        check(r.next() == 2, "read side of empty element: START+END pair");
        check(r.next() == 2 && "root".equals(r.getLocalName()), "END root");
        check(r.next() == 8, "END_DOCUMENT");
        r.close();
    }

    // ---- Deepened 2: writeStartDocument variants and writer construction forms ----
    static void testWriterForms() throws Exception {
        javax.xml.stream.XMLOutputFactory of = javax.xml.stream.XMLOutputFactory.newFactory();
        // no-arg writeStartDocument on Writer
        java.io.StringWriter sw1 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w1 = of.createXMLStreamWriter(sw1);
        w1.writeStartDocument();
        w1.writeStartElement("a");
        w1.writeEndElement();
        w1.writeEndDocument();
        w1.flush();
        check(sw1.toString().equals("<?xml version=\"1.0\" ?><a></a>"), "no-arg startdoc output exact, got [" + sw1 + "]");
        w1.close();
        // version-only startdoc
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w2 = of.createXMLStreamWriter(sw2);
        w2.writeStartDocument("1.0");
        w2.writeEndDocument();
        w2.flush();
        check(sw2.toString().equals("<?xml version=\"1.0\"?>"), "version-only startdoc, got [" + sw2 + "]");
        w2.close();
        // encoding+version startdoc
        java.io.StringWriter sw3 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w3 = of.createXMLStreamWriter(sw3);
        w3.writeStartDocument("ISO-8859-1", "1.1");
        w3.writeEndDocument();
        w3.flush();
        check(sw3.toString().equals("<?xml version=\"1.1\" encoding=\"ISO-8859-1\"?>"), "encoding+version startdoc");
        w3.close();
        // OutputStream default-encoding writer
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLStreamWriter w4 = of.createXMLStreamWriter(bos);
        w4.writeStartDocument();
        w4.writeStartElement("a");
        w4.writeCharacters("中");
        w4.writeEndElement();
        w4.writeEndDocument();
        w4.flush();
        check(bos.toString("UTF-8").equals("<?xml version=\"1.0\" ?><a>中</a>"), "OutputStream default writer UTF-8 exact");
        w4.close();
        // OutputStream UTF-16: writes BOM
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLStreamWriter w5 = of.createXMLStreamWriter(bos2, "UTF-16");
        w5.writeStartDocument("UTF-16", "1.0");
        w5.writeStartElement("a");
        w5.writeCharacters("t");
        w5.writeEndElement();
        w5.writeEndDocument();
        w5.flush();
        byte[] b = bos2.toByteArray();
        check(b.length >= 2 && (b[0] & 0xff) == 0xfe && (b[1] & 0xff) == 0xff, "UTF-16 writer emits BOM fe ff");
        w5.close();
        // misc write forms: DTD, entity ref, PI without data, char[] characters
        java.io.StringWriter sw6 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w6 = of.createXMLStreamWriter(sw6);
        w6.writeStartDocument("1.0");
        w6.writeStartElement("r");
        w6.writeDTD("<!ELEMENT r ANY>");
        w6.writeEntityRef("amp");
        w6.writeProcessingInstruction("solo");
        w6.writeEmptyElement("e");
        w6.writeCharacters(new char[]{'x', 'y', 'z'}, 1, 2);
        w6.writeEndElement();
        w6.writeEndDocument();
        w6.flush();
        check(sw6.toString().equals("<?xml version=\"1.0\"?><r><!ELEMENT r ANY>&amp;<?solo?><e/>yz</r>"),
            "DTD/entityRef/PI/char[] output exact, got [" + sw6 + "]");
        w6.close();
    }

    // ---- Deepened 3: prefix handling, unmatched tags, quirk locks ----
    static void testWriterEdgeCases() throws Exception {
        javax.xml.stream.XMLOutputFactory of = javax.xml.stream.XMLOutputFactory.newFactory();
        // setPrefix/getPrefix round-trip before any element
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = of.createXMLStreamWriter(sw);
        w.setPrefix("pp", "urn:x");
        check("pp".equals(w.getPrefix("urn:x")), "setPrefix/getPrefix round-trip");
        w.setDefaultNamespace("urn:dn");
        check("".equals(w.getPrefix("urn:dn")), "default namespace prefix is \"\"");
        w.writeStartDocument();
        w.writeStartElement("pp", "r", "urn:x");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        check(sw.toString().equals("<?xml version=\"1.0\" ?><pp:r></pp:r>"), "prefix-bound element output, got [" + sw + "]");
        w.close();
        // writeEndDocument auto-closes unclosed elements
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w2 = of.createXMLStreamWriter(sw2);
        w2.writeStartElement("a");
        w2.writeStartElement("b");
        w2.writeEndDocument();
        w2.flush();
        check(sw2.toString().equals("<a><b></b></a>"), "writeEndDocument closes open elements, got [" + sw2 + "]");
        w2.close();
        // close() without end tags leaves output unterminated (no exception)
        java.io.StringWriter sw3 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w3 = of.createXMLStreamWriter(sw3);
        w3.writeStartElement("a");
        w3.writeCharacters("x");
        boolean closed = false;
        try { w3.close(); closed = true; } catch (javax.xml.stream.XMLStreamException e) { closed = false; }
        check(closed && sw3.toString().equals("<a>x"), "close() with open element: no exception, unterminated output");
        // attribute with undeclared namespace (non-repairing): XMLStreamException
        java.io.StringWriter sw4 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w4 = of.createXMLStreamWriter(sw4);
        w4.writeStartElement("r");
        boolean attrErr = false;
        try { w4.writeAttribute("urn:nope", "k", "v"); }
        catch (javax.xml.stream.XMLStreamException e) { attrErr = true; }
        check(attrErr, "writeAttribute(undeclared ns) -> XMLStreamException (non-repairing)");
        w4.close();
        // writer quirk locks (identical across releases): CDATA containing ]]> and comment containing -- pass through raw
        java.io.StringWriter sw5 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w5 = of.createXMLStreamWriter(sw5);
        w5.writeStartElement("a");
        w5.writeCData("x]]>y");
        w5.writeComment("a--b");
        w5.writeEndElement();
        w5.flush();
        check(sw5.toString().equals("<a><![CDATA[x]]>y]]><!--a--b--></a>"),
            "quirk: CDATA ]]> and comment -- are not escaped by the writer");
        w5.close();
        // getProperty unknown -> IllegalArgumentException
        java.io.StringWriter sw6 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w6 = of.createXMLStreamWriter(sw6);
        boolean piae = false;
        try { w6.getProperty("no/such/prop"); }
        catch (IllegalArgumentException e) { piae = true; }
        check(piae, "writer.getProperty(unknown) -> IllegalArgumentException");
        w6.close();
        // setNamespaceContext accepted pre-start (contents not required for non-repairing output)
        java.io.StringWriter sw7 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w7 = of.createXMLStreamWriter(sw7);
        boolean ctxOk = false;
        try {
            w7.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
                public String getNamespaceURI(String p) { return null; }
                public String getPrefix(String ns) { return null; }
                public java.util.Iterator<String> getPrefixes(String ns) { return java.util.Collections.<String>emptyList().iterator(); }
            });
            ctxOk = true;
        } catch (javax.xml.stream.XMLStreamException e) { ctxOk = false; }
        check(ctxOk, "setNamespaceContext accepted before any write");
        w7.close();
    }
}
