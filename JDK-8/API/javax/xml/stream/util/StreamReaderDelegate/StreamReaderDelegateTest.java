/*
 * @test
 * @summary javax.xml.stream.util.StreamReaderDelegate conformance: stream reader delegate.
 * @run main StreamReaderDelegateTest
 */


public class StreamReaderDelegateTest {
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
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate(r);
        check(d.getParent() == r, "parent");
        check(d.hasNext(), "hasNext");
        d.close();
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (override vs passthrough matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        String xml = "<?xml version='1.0'?><r a='1' b='2'><c>txt</c><?p d?> tail</r>";

        // 1) override wins: getLocalName overridden while everything else passes through
        javax.xml.stream.XMLStreamReader parent = ifac.createXMLStreamReader(new java.io.StringReader(xml));
        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate(parent) {
            public String getLocalName() { return "OVR"; }
        };
        check(d.getParent() == parent, "getParent identity");
        d.next(); // START r
        check(d.getEventType() == 1, "passthrough getEventType 1");
        check("OVR".equals(d.getLocalName()), "overridden getLocalName");
        check(d.getName().getLocalPart().equals("r"), "passthrough getName r (not overridden)");
        check(d.getAttributeCount() == 2, "passthrough getAttributeCount 2");
        check("1".equals(d.getAttributeValue(0)) && "2".equals(d.getAttributeValue(1)), "passthrough getAttributeValue(i)");
        check("1".equals(d.getAttributeValue(null, "a")), "passthrough getAttributeValue(ns,l)");
        check("a".equals(d.getAttributeLocalName(0)) && "b".equals(d.getAttributeLocalName(1)), "passthrough getAttributeLocalName(i)");
        check("".equals(d.getAttributePrefix(0)), "passthrough getAttributePrefix empty");
        check("CDATA".equals(d.getAttributeType(0)), "passthrough getAttributeType CDATA");
        check(d.getAttributeNamespace(0) == null, "passthrough getAttributeNamespace null");
        check(d.isAttributeSpecified(0), "passthrough isAttributeSpecified true");
        check(d.getNamespaceURI() == null && "".equals(d.getPrefix()), "passthrough ns null / prefix empty for no-ns element");
        check(d.hasName() && !d.hasText(), "passthrough hasName/hasText");
        check(d.isStartElement() && !d.isEndElement(), "passthrough isStartElement/isEndElement");
        check(d.getLocation().getLineNumber() == 1, "passthrough getLocation line 1");
        check(Boolean.TRUE.equals(d.getProperty("javax.xml.stream.isReplacingEntityReferences")),
                "passthrough getProperty known");
        check(d.getProperty("zzz") == null, "passthrough getProperty unknown null");
        check("1.0".equals(d.getVersion()), "passthrough getVersion 1.0");
        check(d.getEncoding() == null && d.getCharacterEncodingScheme() == null, "passthrough encodings null (Reader)");
        check(!d.isStandalone(), "passthrough isStandalone false (no standalone decl)");
        d.next(); // START c
        d.next(); // CHARACTERS txt
        check("txt".equals(d.getText()), "passthrough getText");
        check(d.getTextLength() == 3, "passthrough getTextLength 3");
        check(new String(d.getTextCharacters(), d.getTextStart(), d.getTextLength()).equals("txt"),
                "passthrough getTextCharacters");
        check(d.isCharacters() && !d.isWhiteSpace(), "passthrough isCharacters/isWhiteSpace");
        d.next(); // END c
        check(d.isEndElement() && "c".equals(d.getName().getLocalPart()), "passthrough end element state");
        d.next(); // PI
        check("p".equals(d.getPITarget()) && "d".equals(d.getPIData()), "passthrough getPITarget/getPIData");
        d.next(); // CHARACTERS tail
        check(!d.isWhiteSpace(), "passthrough non-ws text");
        d.next(); // END r
        d.require(javax.xml.stream.XMLStreamConstants.END_ELEMENT, null, "r");
        passed++; // require(END_ELEMENT, null, "r") passed without exception
        d.next(); // END_DOCUMENT
        check(d.getEventType() == 8 && !d.hasNext(), "passthrough to END_DOCUMENT");
        d.close();

        // 2) constructors: no-arg then setParent vs reader-arg
        javax.xml.stream.util.StreamReaderDelegate d2 = new javax.xml.stream.util.StreamReaderDelegate();
        check(d2.getParent() == null, "no-arg parent null");
        try { d2.hasNext(); check(false, "no-parent hasNext must NPE"); }
        catch (NullPointerException npe) { passed++; }
        try { d2.getEventType(); check(false, "no-parent getEventType must NPE"); }
        catch (NullPointerException npe) { passed++; }
        javax.xml.stream.XMLStreamReader p2 = ifac.createXMLStreamReader(new java.io.StringReader("<r/>"));
        d2.setParent(p2);
        check(d2.getParent() == p2 && d2.hasNext(), "setParent wires reader");
        d2.close();
        javax.xml.stream.util.StreamReaderDelegate d3 =
                new javax.xml.stream.util.StreamReaderDelegate(ifac.createXMLStreamReader(new java.io.StringReader("<r/>")));
        check(d3.getParent() != null, "reader-arg constructor parent");
        d3.close();

        // 3) getElementText passthrough: text content + ends on END_ELEMENT
        javax.xml.stream.util.StreamReaderDelegate d4 = new javax.xml.stream.util.StreamReaderDelegate(
                ifac.createXMLStreamReader(new java.io.StringReader("<c>AB</c>")));
        d4.next();
        check("AB".equals(d4.getElementText()), "getElementText passthrough AB");
        check(d4.getEventType() == 2, "getElementText ends on END_ELEMENT");

        // 4) negative: getElementText in wrong state -> XMLStreamException
        try {
            javax.xml.stream.util.StreamReaderDelegate d5 = new javax.xml.stream.util.StreamReaderDelegate(
                    ifac.createXMLStreamReader(new java.io.StringReader("<r><c/></r>")));
            d5.getElementText();
            check(false, "getElementText wrong state must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("START_ELEMENT"), "wrong-state message mentions START_ELEMENT");
        }

        // 5) negative: nextTag over non-whitespace text -> XMLStreamException
        try {
            javax.xml.stream.util.StreamReaderDelegate d6 = new javax.xml.stream.util.StreamReaderDelegate(
                    ifac.createXMLStreamReader(new java.io.StringReader("<r>abc</r>")));
            d6.next();
            d6.nextTag();
            check(false, "nextTag on text must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("CHARACTERS"), "nextTag message mentions CHARACTERS");
        }

        // 6) nextTag skips inter-element whitespace
        javax.xml.stream.util.StreamReaderDelegate d7 = new javax.xml.stream.util.StreamReaderDelegate(
                ifac.createXMLStreamReader(new java.io.StringReader("<r>  <c/>  </r>")));
        check(d7.nextTag() == 1 && "r".equals(d7.getLocalName()), "nextTag lands on START r");
        check(d7.nextTag() == 1 && "c".equals(d7.getLocalName()), "nextTag skips whitespace to START c");
        check(d7.nextTag() == 2, "nextTag reaches END c");
        d7.close();
    }
}
