/*
 * @test
 * @summary javax.xml.stream.XMLEventWriter conformance: interface hierarchy and methods.
 * @run main XMLEventWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventWriterTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventWriter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.util.XMLEventConsumer"), "implements javax.xml.stream.util.XMLEventConsumer");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("flush"), "has method flush");
        check(names.contains("close"), "has method close");
        check(names.contains("add"), "has method add");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("setPrefix"), "has method setPrefix");
        check(names.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(names.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        testWriteEventsRoundTrip();
        testAddReaderCopy();
        testPrefixesAndEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: factory-made events -> writer -> parse-back closure ----
    static void testWriteEventsRoundTrip() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.XMLOutputFactory of = javax.xml.stream.XMLOutputFactory.newFactory();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter w = of.createXMLEventWriter(bos, "UTF-8");
        w.add(ef.createStartDocument("UTF-8", "1.0"));
        w.add(ef.createStartElement("q", "urn:q", "n",
            java.util.Collections.singletonList((javax.xml.stream.events.Attribute) ef.createAttribute("k", "v")).iterator(),
            java.util.Collections.singletonList((javax.xml.stream.events.Namespace) ef.createNamespace("q", "urn:q")).iterator()));
        w.add(ef.createCharacters("tx"));
        w.add(ef.createCData("cd"));
        w.add(ef.createComment("cm"));
        w.add(ef.createProcessingInstruction("tgt", "pid"));
        w.add(ef.createEntityReference("ent", null));
        w.add(ef.createDTD("<!ELEMENT n ANY>"));
        w.add(ef.createEndElement("q", "urn:q", "n"));
        w.add(ef.createEndDocument());
        w.flush();
        String out = bos.toString("UTF-8");
        String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<q:n xmlns:q=\"urn:q\" k=\"v\">tx<![CDATA[cd]]><!--cm--><?tgt pid?>&ent;<!ELEMENT n ANY></q:n>";
        check(expect.equals(out), "event writer full output exact, got [" + out + "]");
        w.close();
        // parse back: tree, text, PI, entity all equal
        javax.xml.stream.XMLInputFactory inf = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = inf.createXMLEventReader(new java.io.StringReader(out));
        r.nextEvent(); // SD
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        check(se.getName().equals(new javax.xml.namespace.QName("urn:q", "n", "q")), "round-trip element QName");
        check(se.getAttributes().hasNext() && "v".equals(
            ((javax.xml.stream.events.Attribute) se.getAttributes().next()).getValue()), "round-trip attribute");
        StringBuilder text = new StringBuilder();
        while (true) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e.getEventType() == 3) { check("tgt".equals(((javax.xml.stream.events.ProcessingInstruction) e).getTarget()), "round-trip PI"); break; }
            if (e.isCharacters()) text.append(e.asCharacters().getData());
        }
        check("txcd".equals(text.toString()), "round-trip text incl CDATA, got [" + text + "]");
        r.close();
    }

    // ---- Deepened 2: add(XMLEventReader) copies the whole stream ----
    static void testAddReaderCopy() throws Exception {
        javax.xml.stream.XMLInputFactory inf = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader src = inf.createXMLEventReader(new java.io.StringReader(
            "<r x='1'><c>t</c></r>"));
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw);
        w.add(src);
        w.flush();
        String out = sw.toString();
        check(out.contains("<r") && out.contains("x=\"1\"") && out.contains("<c>t</c>") && out.contains("</r>"),
            "add(XMLEventReader) copies whole document, got [" + out + "]");
        check(!src.hasNext(), "source reader exhausted by add(reader)");
        src.close();
        w.close();
        // add of an already-exhausted reader: no output, no exception
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w2 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw2);
        boolean ok = false;
        try { w2.add(src); ok = true; } catch (javax.xml.stream.XMLStreamException e) { ok = false; }
        check(ok && sw2.toString().isEmpty(), "add(exhausted reader) is a no-op");
        w2.close();
        // copy parses back identically (closure)
        javax.xml.stream.XMLEventReader back = inf.createXMLEventReader(new java.io.StringReader(out));
        javax.xml.stream.events.XMLEvent e1 = back.nextEvent(); // SD
        javax.xml.stream.events.XMLEvent e2 = back.nextEvent(); // SE r
        check(e1.isStartDocument() && "r".equals(e2.asStartElement().getName().getLocalPart()), "copied output reparses");
        back.close();
    }

    // ---- Deepened 3: prefix/default-ns API + edge behaviors ----
    static void testPrefixesAndEdgeCases() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        // namespace context round-trip through the writer
        javax.xml.stream.XMLEventWriter w = javax.xml.stream.XMLOutputFactory.newFactory()
            .createXMLEventWriter(new java.io.StringWriter());
        w.setDefaultNamespace("urn:d");
        check("urn:d".equals(w.getNamespaceContext().getNamespaceURI("")), "setDefaultNamespace visible in context");
        w.setPrefix("p", "urn:p");
        check("p".equals(w.getPrefix("urn:p")), "setPrefix/getPrefix round-trip");
        w.close();
        // mismatched EndElement name is accepted silently (writer does not validate balance)
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w2 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw);
        boolean ok = false;
        try {
            w2.add(ef.createStartDocument());
            w2.add(ef.createStartElement("", "", "a"));
            w2.add(ef.createEndElement("", "", "zzz"));
            w2.add(ef.createEndDocument());
            w2.flush();
            ok = true;
        } catch (javax.xml.stream.XMLStreamException e) { ok = false; }
        check(ok && sw.toString().contains("</a>"), "mismatched EndElement name written as open element's name");
        w2.close();
        // orphan Characters before any root: written raw
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w3 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw2);
        boolean ok2 = false;
        try { w3.add(ef.createCharacters("orphan")); w3.flush(); ok2 = true; }
        catch (javax.xml.stream.XMLStreamException e) { ok2 = false; }
        check(ok2 && "orphan".equals(sw2.toString()), "Characters before root written verbatim");
        w3.close();
        // flush is idempotent (complete element pair)
        java.io.StringWriter sw3 = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w4 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw3);
        w4.add(ef.createStartElement("", "", "x"));
        w4.add(ef.createEndElement("", "", "x"));
        w4.flush();
        w4.flush();
        check(sw3.toString().equals("<x></x>"), "double flush keeps output stable, got [" + sw3 + "]");
        w4.close();
    }
}
