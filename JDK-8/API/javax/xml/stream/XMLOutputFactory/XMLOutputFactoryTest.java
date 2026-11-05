/*
 * @test
 * @summary javax.xml.stream.XMLOutputFactory conformance: XML output factory.
 * @run main XMLOutputFactoryTest
 */


public class XMLOutputFactoryTest {
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
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        check(f != null, "factory non-null");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(sw);
        w.writeStartDocument();
        w.writeStartElement("a");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        check(sw.toString().indexOf("<a") >= 0, "wrote element");
        testPropertyContract();
        testWriterCreation();
        testRepairingNamespaces();
        testFactoryIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: property contract ----
    static void testPropertyContract() throws Exception {
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        check(f.isPropertySupported(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES),
            "IS_REPAIRING_NAMESPACES supported");
        check(!f.isPropertySupported("no/such/prop"), "unknown property unsupported");
        check(Boolean.FALSE.equals(f.getProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES)),
            "IS_REPAIRING_NAMESPACES default false");
        f.setProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        check(Boolean.TRUE.equals(f.getProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES)),
            "IS_REPAIRING_NAMESPACES true round-trip");
        f.setProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
        check(Boolean.FALSE.equals(f.getProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES)),
            "IS_REPAIRING_NAMESPACES false round-trip");
        check(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals("javax.xml.stream.isRepairingNamespaces"),
            "IS_REPAIRING_NAMESPACES property name constant");
        boolean iae = false;
        try { f.setProperty("no/such/prop", Boolean.TRUE); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setProperty(unknown) -> IllegalArgumentException");
        boolean iae2 = false;
        try { f.getProperty("no/such/prop"); }
        catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "getProperty(unknown) -> IllegalArgumentException");
    }

    // ---- Deepened 2: writer construction forms ----
    static void testWriterCreation() throws Exception {
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        // Writer
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w1 = f.createXMLStreamWriter(sw);
        w1.writeStartDocument("UTF-8", "1.0");
        w1.writeStartElement("x");
        w1.writeAttribute("k", "v");
        w1.writeCharacters("hi");
        w1.writeEndElement();
        w1.writeEndDocument();
        w1.close();
        check("<?xml version=\"1.0\" encoding=\"UTF-8\"?><x k=\"v\">hi</x>".equals(sw.toString()),
            "Writer-based stream writer exact output, got [" + sw + "]");
        // OutputStream + encoding
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLStreamWriter w2 = f.createXMLStreamWriter(bos, "UTF-8");
        w2.writeStartDocument("UTF-8", "1.0");
        w2.writeStartElement("x");
        w2.writeAttribute("k", "v");
        w2.writeCharacters("hi");
        w2.writeEndElement();
        w2.writeEndDocument();
        w2.close();
        check("<?xml version=\"1.0\" encoding=\"UTF-8\"?><x k=\"v\">hi</x>".equals(bos.toString("UTF-8")),
            "OutputStream-based stream writer exact output");
        // OutputStream no encoding
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLStreamWriter w3 = f.createXMLStreamWriter(bos2);
        w3.writeStartDocument();
        w3.writeStartElement("y");
        w3.writeEndDocument();
        w3.flush();
        check(bos2.toString("UTF-8").startsWith("<?xml version=\"1.0\" ?><y>"), "OutputStream default writer starts with decl");
        w3.close();
        // EventWriter forms
        java.io.StringWriter esw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter ew1 = f.createXMLEventWriter(esw);
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        ew1.add(ef.createStartDocument());
        ew1.add(ef.createStartElement("", "", "e"));
        ew1.add(ef.createEndElement("", "", "e"));
        ew1.add(ef.createEndDocument());
        ew1.close();
        check(esw.toString().contains("<e") && esw.toString().contains("</e>"), "EventWriter over Writer output");
        java.io.ByteArrayOutputStream ebos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew2 = f.createXMLEventWriter(ebos, "UTF-8");
        ew2.add(ef.createStartDocument("UTF-8", "1.0"));
        ew2.add(ef.createStartElement("", "", "e"));
        ew2.add(ef.createEndElement("", "", "e"));
        ew2.add(ef.createEndDocument());
        ew2.close();
        check(ebos.toString("UTF-8").equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><e></e>"),
            "EventWriter over OutputStream+encoding exact, got [" + ebos.toString("UTF-8") + "]");
    }

    // ---- Deepened 3: repairing namespaces ----
    static void testRepairingNamespaces() throws Exception {
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        f.setProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        // undeclared prefix auto-bound
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(sw);
        w.writeStartElement("q", "e", "urn:q");
        w.writeAttribute("q", "urn:q", "a", "1");
        w.writeCharacters("t");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        check("<q:e xmlns:q=\"urn:q\" q:a=\"1\">t</q:e>".equals(sw.toString()),
            "repairing auto-declares undeclared prefix, got [" + sw + "]");
        w.close();
        // duplicate declaration suppressed
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w2 = f.createXMLStreamWriter(sw2);
        w2.writeStartElement("a");
        w2.writeNamespace("p", "urn:p");
        w2.writeStartElement("p", "b", "urn:p");
        w2.writeNamespace("p", "urn:p"); // duplicate binding
        w2.writeEndElement();
        w2.writeEndElement();
        w2.writeEndDocument();
        w2.flush();
        check("<a xmlns:p=\"urn:p\"><p:b></p:b></a>".equals(sw2.toString()),
            "repairing suppresses redundant re-declaration, got [" + sw2 + "]");
        w2.close();
        // repairing output parses back (closure)
        javax.xml.stream.XMLInputFactory inf = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = inf.createXMLStreamReader(new java.io.StringReader(sw2.toString()));
        r.next();
        check("a".equals(r.getLocalName()) && r.getNamespaceURI() == null,
            "repairing output round-trips (root without default ns -> null ns URI)");
        check(r.next() == 1 && "urn:p".equals(r.getNamespaceURI()) && "p".equals(r.getPrefix()),
            "repairing output round-trips (child keeps bound ns)");
        r.close();
    }

    // ---- Deepened 4: factory identity and event writer prefix API ----
    static void testFactoryIdentity() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.XMLEventWriter w = javax.xml.stream.XMLOutputFactory.newFactory()
            .createXMLEventWriter(new java.io.StringWriter());
        w.setDefaultNamespace("urn:d");
        check("urn:d".equals(w.getNamespaceContext().getNamespaceURI("")), "event writer setDefaultNamespace reflected in context");
        w.setPrefix("p", "urn:p");
        check("p".equals(w.getPrefix("urn:p")), "event writer setPrefix/getPrefix round-trip");
        w.close();
    }
}
