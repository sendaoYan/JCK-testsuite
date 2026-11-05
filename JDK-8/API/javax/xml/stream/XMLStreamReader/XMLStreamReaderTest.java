/*
 * @test
 * @summary javax.xml.stream.XMLStreamReader conformance: interface hierarchy and methods.
 * @run main XMLStreamReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamReaderTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.XMLStreamConstants"), "implements javax.xml.stream.XMLStreamConstants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("next"), "has method next");
        check(names.contains("require"), "has method require");
        check(names.contains("getElementText"), "has method getElementText");
        check(names.contains("nextTag"), "has method nextTag");
        check(names.contains("hasNext"), "has method hasNext");
        check(names.contains("close"), "has method close");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(names.contains("isStartElement"), "has method isStartElement");
        check(names.contains("isEndElement"), "has method isEndElement");
        check(names.contains("isCharacters"), "has method isCharacters");
        check(names.contains("isWhiteSpace"), "has method isWhiteSpace");
        check(names.contains("getAttributeValue"), "has method getAttributeValue");
        check(names.contains("getAttributeCount"), "has method getAttributeCount");
        check(names.contains("getAttributeName"), "has method getAttributeName");
        check(names.contains("getAttributeNamespace"), "has method getAttributeNamespace");
        check(names.contains("getAttributeLocalName"), "has method getAttributeLocalName");
        check(names.contains("getAttributePrefix"), "has method getAttributePrefix");
        check(names.contains("getAttributeType"), "has method getAttributeType");
        check(names.contains("isAttributeSpecified"), "has method isAttributeSpecified");
        check(names.contains("getNamespaceCount"), "has method getNamespaceCount");
        check(names.contains("getNamespacePrefix"), "has method getNamespacePrefix");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("getEventType"), "has method getEventType");
        check(names.contains("getText"), "has method getText");
        check(names.contains("getTextCharacters"), "has method getTextCharacters");
        check(names.contains("getTextStart"), "has method getTextStart");
        check(names.contains("getTextLength"), "has method getTextLength");
        check(names.contains("getEncoding"), "has method getEncoding");
        check(names.contains("hasText"), "has method hasText");
        check(names.contains("getLocation"), "has method getLocation");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getLocalName"), "has method getLocalName");
        check(names.contains("hasName"), "has method hasName");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("getVersion"), "has method getVersion");
        check(names.contains("isStandalone"), "has method isStandalone");
        check(names.contains("standaloneSet"), "has method standaloneSet");
        check(names.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(names.contains("getPITarget"), "has method getPITarget");
        check(names.contains("getPIData"), "has method getPIData");
        testEventStateMachine();
        testElementTextAndNextTag();
        testLifecycleAndNegative();
        testNamespacesAndAttributes();
        testLocationAndProperties();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: full event state machine over a fixed document ----
    static void testEventStateMachine() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<?proc inst?>\n"
            + "<!--top comment-->\n"
            + "<p:root xmlns=\"http://def\" xmlns:p=\"http://p\" p:attr=\"v1\" plain=\"v2\">\n"
            + "  text&amp;more&lt;\n"
            + "  <child><![CDATA[cdata <content> here]]></child>\n"
            + "  <empty/>\n"
            + "  &#65;&#x4E2D;\n"
            + "</p:root>\n";
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(xml));
        // initial state: START_DOCUMENT with XMLDecl round-trip facts
        check(r.getEventType() == 7, "initial event START_DOCUMENT(7)");
        check("1.0".equals(r.getVersion()), "getVersion from XMLDecl");
        check("UTF-8".equals(r.getCharacterEncodingScheme()), "getCharacterEncodingScheme from XMLDecl");
        check(r.getEncoding() == null, "getEncoding null for Reader input (no concrete stream encoding)");
        check(r.isStandalone() && r.standaloneSet(), "standalone=yes round-trip");
        check(r.hasNext(), "hasNext at START_DOCUMENT");
        // full next() sequence locked
        java.util.List<Integer> expect = java.util.Arrays.asList(
            3, 5, 1, 4, 4, 4, 4, 4, 1, 4, 2, 4, 1, 2, 4, 4, 4, 4, 2, 8);
        java.util.List<Integer> got = new java.util.ArrayList<Integer>();
        while (r.hasNext()) got.add(r.next());
        check(got.equals(expect), "event sequence " + expect + ", got " + got);
        check(!r.hasNext(), "hasNext false after END_DOCUMENT");
        r.close();

        // per-event facts: PI, comment, names, entity split, char refs
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader(xml));
        r2.next(); // PI
        check(r2.getEventType() == 3 && "proc".equals(r2.getPITarget()) && "inst".equals(r2.getPIData()),
            "PI target/data");
        r2.next(); // COMMENT
        check(r2.getEventType() == 5 && "top comment".equals(r2.getText()), "comment text");
        r2.next(); // START root
        check(r2.isStartElement() && !r2.isEndElement() && !r2.isCharacters(), "isStartElement family");
        check(r2.hasName() && !r2.hasText(), "START hasName and no text");
        javax.xml.namespace.QName rootName = r2.getName();
        check("root".equals(rootName.getLocalPart()) && "http://p".equals(rootName.getNamespaceURI())
            && "p".equals(rootName.getPrefix()), "root QName {http://p}root, prefix p");
        check("root".equals(r2.getLocalName()) && "http://p".equals(r2.getNamespaceURI()) && "p".equals(r2.getPrefix()),
            "getLocalName/getNamespaceURI/getPrefix");
        check(r2.getAttributeCount() == 2, "2 real attributes (xmlns decls excluded)");
        check(r2.getAttributeName(0).equals(new javax.xml.namespace.QName("http://p", "attr", "p")), "attr[0] QName");
        check("v1".equals(r2.getAttributeValue(0)) && "v1".equals(r2.getAttributeValue("http://p", "attr")),
            "getAttributeValue(index) and (ns,local)");
        check("v2".equals(r2.getAttributeValue(1)) && "plain".equals(r2.getAttributeLocalName(1))
            && "v2".equals(r2.getAttributeValue(null, "plain")), "unprefixed attr lookup (null ns)");
        check(r2.isAttributeSpecified(0) && r2.isAttributeSpecified(1), "explicitly specified attrs");
        check("CDATA".equals(r2.getAttributeType(0)), "attribute type CDATA default");
        check(r2.getAttributeNamespace(0).equals("http://p") && r2.getAttributePrefix(0).equals("p"), "attr ns/prefix by index");
        check("v1".equals(r2.getAttributeValue(null, "attr")),
            "null ns in getAttributeValue matches any namespace (JDK impl ignores null ns)");
        // entity splitting: text, &, more, < each separate CHARACTERS (no coalescing)
        StringBuilder merged = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            check(r2.next() == 4, "CHARACTERS run " + i);
            merged.append(r2.getText());
        }
        check("\n  text&more<\n  ".equals(merged.toString()), "entity split concatenates to full text, got [" + merged + "]");
        check(r2.getTextCharacters().length >= r2.getTextStart() + r2.getTextLength(), "getTextCharacters buffer covers text");
        // CDATA content delivered as CHARACTERS with decoded body (JDK stream behavior, both releases)
        r2.next(); // START child
        check("{http://def}child".equals(r2.getName().toString()), "default ns applies to child: {http://def}child");
        check("".equals(r2.getPrefix()), "default ns means empty prefix");
        check(r2.next() == 4 && "cdata <content> here".equals(r2.getText()), "CDATA body as CHARACTERS text");
        r2.next(); // END child
        check(r2.isEndElement() && r2.hasName() && "child".equals(r2.getLocalName()), "END child");
        r2.next(); // whitespace before <empty/>
        check(r2.getEventType() == 4 && r2.isWhiteSpace(), "inter-element whitespace is CHARACTERS+isWhiteSpace");
        check(r2.next() == 1 && "empty".equals(r2.getLocalName()), "START empty");
        check(r2.next() == 2 && "empty".equals(r2.getLocalName()), "empty element yields START then END pair");
        r2.next(); // whitespace after
        check(r2.isWhiteSpace(), "trailing ws after empty");
        // character references decoded
        r2.next(); // CHARACTERS A
        check("A".equals(r2.getText()), "decimal char ref &#65; decodes to A");
        check(r2.next() == 4 && "中".equals(r2.getText()), "hex char ref &#x4E2D; decodes to U+4E2D 中");
        r2.next(); // trailing whitespace before END root
        check(r2.isWhiteSpace(), "whitespace before END root");
        r2.next(); // END root
        check(r2.isEndElement() && "root".equals(r2.getLocalName()), "END root");
        r2.next(); // END_DOCUMENT
        check(r2.getEventType() == 8, "END_DOCUMENT(8)");
        r2.close();
    }

    // ---- Deepened 2: getElementText / nextTag contracts ----
    static void testElementTextAndNextTag() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        // entity + CDATA + charref concatenation; cursor lands on END_ELEMENT
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<e>a&amp;b<![CDATA[<c>]]>&#65;&#x4E2D;</e>"));
        r.next();
        String t = r.getElementText();
        check("a&b<c>A中".equals(t), "getElementText merges entity/CDATA/charref, got [" + t + "]");
        check(r.getEventType() == 2, "cursor on END_ELEMENT after getElementText");
        r.close();
        // comments and PIs inside element are skipped by getElementText
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader(
            "<e>a<!--c-->b<?pi d?>c</e>"));
        r2.next();
        check("abc".equals(r2.getElementText()), "getElementText skips comment and PI");
        r2.close();
        // getElementText on wrong state: JDK throws XMLStreamException (not the javadoc IllegalStateException)
        javax.xml.stream.XMLStreamReader r3 = f.createXMLStreamReader(new java.io.StringReader("<e>txt</e>"));
        r3.next(); r3.next(); // CHARACTERS
        boolean xmlse = false;
        try { r3.getElementText(); }
        catch (IllegalStateException e) { xmlse = true; } // tolerated per javadoc, not observed on JDK-8
        catch (javax.xml.stream.XMLStreamException e) { xmlse = true; }
        check(xmlse, "getElementText off START_ELEMENT throws");
        r3.close();
        // nextTag skips whitespace and comments and PIs
        javax.xml.stream.XMLStreamReader r4 = f.createXMLStreamReader(new java.io.StringReader(
            "<a>\n  <!--c--> <?pi d?> <b/> </a>"));
        check(r4.nextTag() == 1 && "a".equals(r4.getLocalName()), "nextTag to root");
        check(r4.nextTag() == 1 && "b".equals(r4.getLocalName()), "nextTag skips ws/comment/PI");
        check(r4.nextTag() == 2, "nextTag to END of parent");
        r4.close();
        // nextTag over non-whitespace text -> XMLStreamException
        javax.xml.stream.XMLStreamReader r5 = f.createXMLStreamReader(new java.io.StringReader("<a>x</a>"));
        r5.next();
        boolean caught = false;
        try { r5.nextTag(); }
        catch (javax.xml.stream.XMLStreamException e) { caught = true; }
        check(caught, "nextTag over non-ws text throws XMLStreamException");
        r5.close();
        // require(): match ok, mismatch throws
        javax.xml.stream.XMLStreamReader r6 = f.createXMLStreamReader(new java.io.StringReader(
            "<n:a xmlns:n='urn:n'><b/></n:a>"));
        r6.next();
        r6.require(1, "urn:n", "a"); // must not throw
        check(true, "require match passes");
        boolean reqFail = false;
        try { r6.require(1, "urn:n", "zzz"); }
        catch (javax.xml.stream.XMLStreamException e) { reqFail = true; }
        check(reqFail, "require mismatch throws XMLStreamException");
        r6.close();
    }

    // ---- Deepened 3: lifecycle (close) and negative paths ----
    static void testLifecycleAndNegative() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        // next() beyond END_DOCUMENT -> NoSuchElementException
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        while (r.hasNext()) r.next();
        check(!r.hasNext(), "hasNext false at exhaustion");
        boolean nsee = false;
        try { r.next(); }
        catch (java.util.NoSuchElementException e) { nsee = true; }
        check(nsee, "next() past END_DOCUMENT throws NoSuchElementException");
        r.close();
        // close() then next(): JDK keeps parsing (close is advisory here)
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader("<a><b/></a>"));
        r2.next();
        r2.close();
        boolean okAfterClose = false;
        try { okAfterClose = r2.next() == 1; } catch (Throwable t) { okAfterClose = false; }
        check(okAfterClose, "JDK-8: next() after close() continues parsing (locked empirical)");
        // well-formedness error -> XMLStreamException with Location
        boolean parseErr = false;
        javax.xml.stream.XMLStreamReader r3 = f.createXMLStreamReader(new java.io.StringReader("<a><b></a>"));
        try { while (r3.hasNext()) r3.next(); }
        catch (javax.xml.stream.XMLStreamException e) {
            parseErr = e.getLocation() != null && e.getLocation().getLineNumber() >= 1;
        }
        check(parseErr, "malformed doc -> XMLStreamException with Location line >= 1");
        if (r3 != null) { try { r3.close(); } catch (Throwable ignore) {} }
        // getProperty on reader: unknown returns null (no IAE on this impl)
        javax.xml.stream.XMLStreamReader r4 = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        check(r4.getProperty("no/such/prop") == null, "reader.getProperty(unknown) returns null on JDK impl");
        r4.close();
        // state-guard: getPITarget outside PI -> IllegalStateException
        javax.xml.stream.XMLStreamReader r5 = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        r5.next();
        boolean ise = false;
        try { r5.getPITarget(); }
        catch (IllegalStateException e) { ise = true; }
        check(ise, "getPITarget outside PI event throws IllegalStateException");
        r5.close();
        // PI without data
        javax.xml.stream.XMLStreamReader r6 = f.createXMLStreamReader(new java.io.StringReader("<a><?solo?></a>"));
        r6.next();
        check(r6.next() == 3 && "solo".equals(r6.getPITarget()) && "".equals(r6.getPIData()),
            "PI without data: empty data string");
        r6.close();
        // DTD event with internal subset text
        javax.xml.stream.XMLStreamReader r7 = f.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE r [<!ENTITY e \"RR\">]><r>&e;</r>"));
        check(r7.next() == 11, "DTD event code 11");
        check(r7.getText().equals("<!DOCTYPE r [<!ENTITY e \"RR\">]>"), "DTD text is full doctype decl");
        check(r7.next() == 1 && "r".equals(r7.getLocalName()), "root after DTD");
        check(r7.next() == 4 && "RR".equals(r7.getText()), "internal entity replaced by default");
        r7.close();
    }

    // ---- Deepened 4: namespaces and attribute/namespace matrix ----
    static void testNamespacesAndAttributes() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<p:root xmlns:p='urn:p' xmlns='urn:d'><p:kid/></p:root>"));
        r.next();
        javax.xml.namespace.NamespaceContext ctx = r.getNamespaceContext();
        check("urn:p".equals(ctx.getNamespaceURI("p")), "NamespaceContext lookup p");
        check("urn:d".equals(ctx.getNamespaceURI("")), "NamespaceContext default ns");
        check("http://www.w3.org/XML/1998/namespace".equals(ctx.getNamespaceURI("xml")), "implicit xml prefix");
        check(ctx.getNamespaceURI("nosuch") == null, "unknown prefix -> null");
        check(r.getNamespaceCount() == 2, "2 namespace declarations on root");
        check("urn:p".equals(r.getNamespaceURI("p")), "reader.getNamespaceURI(prefix)");
        java.util.Set<String> prefixes = new java.util.HashSet<String>();
        for (int i = 0; i < r.getNamespaceCount(); i++) prefixes.add(r.getNamespacePrefix(i));
        check(prefixes.contains("p"), "declared prefix p among " + prefixes);
        check(prefixes.contains(null) || prefixes.contains(""), "default ns prefix (null or \"\") among " + prefixes);
        r.next(); // whitespace
        r.next(); // kid
        check("urn:p".equals(r.getNamespaceURI()) && "p".equals(r.getPrefix()), "inherited ns on kid");
        check(r.getNamespaceCount() == 0, "no new decls on kid");
        r.close();
        // isWhiteSpace: text nodes
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader("<a> </a>"));
        r2.next(); r2.next();
        check(r2.getEventType() == 4 && r2.isWhiteSpace(), "whitespace-only CHARACTERS isWhiteSpace true");
        r2.close();
        javax.xml.stream.XMLStreamReader r3 = f.createXMLStreamReader(new java.io.StringReader("<a>x</a>"));
        r3.next(); r3.next();
        check(!r3.isWhiteSpace(), "non-ws text isWhiteSpace false");
        r3.close();
    }

    // ---- Deepened 5: Location and property details ----
    static void testLocationAndProperties() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<r>\n  <a/>\n</r>"));
        r.next(); r.next(); r.next(); // root SE, ws, a SE
        javax.xml.stream.Location loc = r.getLocation();
        check(loc.getLineNumber() == 2, "multi-line doc: element a on line 2, got " + loc.getLineNumber());
        check(loc.getColumnNumber() >= 1, "column >= 1");
        check(loc.getCharacterOffset() == 10, "character offset 10, got " + loc.getCharacterOffset());
        check(loc.getPublicId() == null && loc.getSystemId() == null, "no ids for Reader input");
        String s = String.valueOf(loc);
        check(s.contains("Line number = 2") && s.contains("Column number = "), "Location.toString contains line/col fields");
        r.close();
        // coalescing contrast (behavior belongs to factory but observed through the reader)
        javax.xml.stream.XMLInputFactory fc = javax.xml.stream.XMLInputFactory.newFactory();
        fc.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        javax.xml.stream.XMLStreamReader rc = fc.createXMLStreamReader(new java.io.StringReader(
            "<a>x<![CDATA[y]]>z&amp;w</a>"));
        rc.next();
        check(rc.next() == 4 && "xyz&w".equals(rc.getText()), "IS_COALESCING merges text+CDATA+entity");
        rc.close();
    }
}
