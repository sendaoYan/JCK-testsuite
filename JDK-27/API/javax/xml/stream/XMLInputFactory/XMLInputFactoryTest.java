/*
 * @test
 * @summary javax.xml.stream.XMLInputFactory conformance: white-box structure + black-box behavior.
 * @run main XMLInputFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLInputFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLInputFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "createFilteredReader"), "method createFilteredReader");
        check(hasMethod(c, "createXMLEventReader"), "method createXMLEventReader");
        check(hasMethod(c, "createXMLStreamReader"), "method createXMLStreamReader");
        check(hasMethod(c, "getEventAllocator"), "method getEventAllocator");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getXMLReporter"), "method getXMLReporter");
        check(hasMethod(c, "getXMLResolver"), "method getXMLResolver");
        check(hasMethod(c, "isPropertySupported"), "method isPropertySupported");
        check(hasMethod(c, "newDefaultFactory"), "method newDefaultFactory");
        check(hasMethod(c, "newFactory"), "method newFactory");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "setEventAllocator"), "method setEventAllocator");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setXMLReporter"), "method setXMLReporter");
        check(hasMethod(c, "setXMLResolver"), "method setXMLResolver");
        check(hasField(c, "ALLOCATOR"), "field ALLOCATOR");
        check(hasField(c, "IS_COALESCING"), "field IS_COALESCING");
        check(hasField(c, "IS_NAMESPACE_AWARE"), "field IS_NAMESPACE_AWARE");
        check(hasField(c, "IS_REPLACING_ENTITY_REFERENCES"), "field IS_REPLACING_ENTITY_REFERENCES");
        check(hasField(c, "IS_SUPPORTING_EXTERNAL_ENTITIES"), "field IS_SUPPORTING_EXTERNAL_ENTITIES");
        check(hasField(c, "IS_VALIDATING"), "field IS_VALIDATING");
        check(hasField(c, "REPORTER"), "field REPORTER");
        check(hasField(c, "RESOLVER"), "field RESOLVER");
        check(hasField(c, "SUPPORT_DTD"), "field SUPPORT_DTD");

        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        check(r.hasNext(), "hasNext");
        check(r.next() == javax.xml.stream.XMLStreamConstants.START_ELEMENT, "START_ELEMENT");
        check("a".equals(r.getLocalName()), "localName");
        r.close();
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        check(f != null, "newFactory");
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<root><item id='a'>one</item><item id='b'>two</item></root>"));
        StringBuilder events = new StringBuilder();
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == javax.xml.stream.XMLStreamConstants.START_ELEMENT) events.append(r.getLocalName()).append(";");
        }
        r.close();
        check("root;item;item;".equals(events.toString()), "stream reader start elements, got " + events);
        // XMLEventReader
        javax.xml.stream.XMLEventReader er = f.createXMLEventReader(new java.io.StringReader("<root/>"));
        check(er.hasNext() && er.nextEvent().isStartDocument(), "event reader first event start document");
        check(er.nextEvent().isStartElement(), "event reader second event start element");
        er.close();
        // property
        check(f.isPropertySupported(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE), "IS_NAMESPACE_AWARE supported");
        check(!f.isPropertySupported("http://example.invalid/nope"), "unknown property unsupported");
        boolean ok = false;
        try { f.getProperty("http://example.invalid/nope"); }
        catch (IllegalArgumentException e) { ok = true; }
        check(ok, "getProperty(unknown) -> IllegalArgumentException");
        check(javax.xml.stream.XMLInputFactory.newInstance() != null, "newInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPropertyContract();
        testReaderCreation();
        testBehaviorSwitches();
        testFilteredReaders();
        testFactoryIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: set/getProperty round-trips and error contract ----
    static void testPropertyContract() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        String[] boolProps = {
            javax.xml.stream.XMLInputFactory.IS_COALESCING,
            javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE,
            javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
            javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
            javax.xml.stream.XMLInputFactory.SUPPORT_DTD,
        };
        for (String p : boolProps) {
            check(f.isPropertySupported(p), "isPropertySupported(" + p + ")");
            f.setProperty(p, Boolean.TRUE);
            check(Boolean.TRUE.equals(f.getProperty(p)), "setProperty true round-trip " + p);
            f.setProperty(p, Boolean.FALSE);
            check(Boolean.FALSE.equals(f.getProperty(p)), "setProperty false round-trip " + p);
        }
        check(f.isPropertySupported(javax.xml.stream.XMLInputFactory.IS_VALIDATING), "IS_VALIDATING supported flag");
        f.setProperty(javax.xml.stream.XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        check(Boolean.FALSE.equals(f.getProperty(javax.xml.stream.XMLInputFactory.IS_VALIDATING)), "IS_VALIDATING=false ok");
        // StAX in JDK never validates: enabling throws IllegalArgumentException (locked, both releases)
        boolean iae = false;
        try { f.setProperty(javax.xml.stream.XMLInputFactory.IS_VALIDATING, Boolean.TRUE); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "IS_VALIDATING=true -> IllegalArgumentException (validation unsupported)");
        // unknown property: setProperty and getProperty both reject
        boolean iae2 = false;
        try { f.setProperty("no/such/prop", Boolean.TRUE); }
        catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "setProperty(unknown) -> IllegalArgumentException");
        boolean iae3 = false;
        try { f.getProperty("no/such/prop"); }
        catch (IllegalArgumentException e) { iae3 = true; }
        check(iae3, "getProperty(unknown) -> IllegalArgumentException");
        check(!f.isPropertySupported("no/such/prop"), "isPropertySupported(unknown) false");
        check(javax.xml.stream.XMLInputFactory.IS_COALESCING.equals("javax.xml.stream.isCoalescing"), "IS_COALESCING name");
        check(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE.equals("javax.xml.stream.isNamespaceAware"), "IS_NAMESPACE_AWARE name");
        check(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES.equals("javax.xml.stream.isReplacingEntityReferences"), "IS_REPLACING_ENTITY_REFERENCES name");
        check(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES.equals("javax.xml.stream.isSupportingExternalEntities"), "IS_SUPPORTING_EXTERNAL_ENTITIES name");
        check(javax.xml.stream.XMLInputFactory.IS_VALIDATING.equals("javax.xml.stream.isValidating"), "IS_VALIDATING name");
        check(javax.xml.stream.XMLInputFactory.SUPPORT_DTD.equals("javax.xml.stream.supportDTD"), "SUPPORT_DTD name");
        check(javax.xml.stream.XMLInputFactory.REPORTER.equals("javax.xml.stream.reporter"), "REPORTER name");
        check(javax.xml.stream.XMLInputFactory.RESOLVER.equals("javax.xml.stream.resolver"), "RESOLVER name");
        check(javax.xml.stream.XMLInputFactory.ALLOCATOR.equals("javax.xml.stream.allocator"), "ALLOCATOR name");
    }

    // ---- Deepened 2: createXMLStreamReader/XMLEventReader overloads ----
    static void testReaderCreation() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        // Reader input: decl encoding visible as scheme only
        javax.xml.stream.XMLStreamReader r1 = f.createXMLStreamReader(new java.io.StringReader(
            "<?xml version='1.0' encoding='UTF-8'?><a/>"));
        check(r1.getEventType() == 7, "Reader input initial START_DOCUMENT");
        check("1.0".equals(r1.getVersion()) && "UTF-8".equals(r1.getCharacterEncodingScheme()),
            "decl version/scheme visible");
        check(r1.getEncoding() == null, "Reader input: no stream encoding");
        r1.close();
        // InputStream input: real encoding detected
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.ByteArrayInputStream(
            "<?xml version='1.0' encoding='UTF-8'?><a/>".getBytes("UTF-8")));
        check("UTF-8".equals(r2.getEncoding()), "InputStream input: getEncoding detected UTF-8");
        r2.close();
        // (InputStream, encoding) hint: encoding set, scheme null (no decl)
        javax.xml.stream.XMLStreamReader r3 = f.createXMLStreamReader(new java.io.ByteArrayInputStream(
            "<a/>".getBytes("UTF-8")), "UTF-8");
        check("UTF-8".equals(r3.getEncoding()) && r3.getCharacterEncodingScheme() == null,
            "(InputStream, enc) hint sets encoding, no scheme");
        r3.close();
        // Source input
        javax.xml.stream.XMLStreamReader r4 = f.createXMLStreamReader(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>")));
        check(r4.getEventType() == 7 && r4.hasNext(), "Source input reader usable");
        r4.close();
        // XMLEventReader from Reader / (systemId, Reader) / Source
        javax.xml.stream.XMLEventReader e1 = f.createXMLEventReader(new java.io.StringReader("<a/>"));
        check(e1.nextEvent().getEventType() == 7, "event reader from Reader");
        e1.close();
        javax.xml.stream.XMLEventReader e2 = f.createXMLEventReader("sysid-x",
            new java.io.StringReader("<a/>"));
        check(e2.nextEvent().isStartDocument(), "event reader (systemId, Reader)");
        e2.close();
        javax.xml.stream.XMLEventReader e3 = f.createXMLEventReader(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>")));
        check(e3.nextEvent().isStartDocument(), "event reader from Source");
        e3.close();
        // malformed input rejected at parse time
        boolean thrown = false;
        javax.xml.stream.XMLStreamReader r5 = f.createXMLStreamReader(new java.io.StringReader("<a>"));
        try { while (r5.hasNext()) r5.next(); }
        catch (javax.xml.stream.XMLStreamException e) { thrown = true; }
        check(thrown, "truncated document -> XMLStreamException during iteration");
    }

    // ---- Deepened 3: behavioral property switches ----
    static void testBehaviorSwitches() throws Exception {
        // coalescing: text+CDATA+entity become one CHARACTERS
        javax.xml.stream.XMLInputFactory fc = javax.xml.stream.XMLInputFactory.newFactory();
        fc.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        javax.xml.stream.XMLStreamReader rc = fc.createXMLStreamReader(new java.io.StringReader(
            "<a>x<![CDATA[y]]>z&amp;w</a>"));
        rc.next();
        check(rc.next() == 4 && "xyz&w".equals(rc.getText()), "IS_COALESCING=true merges runs");
        check(rc.next() == 2 && rc.next() == 8, "coalesced doc tail: END_ELEMENT then END_DOCUMENT");
        rc.close();
        // namespace awareness off: names not split
        javax.xml.stream.XMLInputFactory fn = javax.xml.stream.XMLInputFactory.newFactory();
        fn.setProperty(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        javax.xml.stream.XMLStreamReader rn = fn.createXMLStreamReader(new java.io.StringReader(
            "<p:a xmlns:p='urn:p'/>"));
        rn.next();
        check("p:a".equals(rn.getLocalName()) && rn.getNamespaceURI() == null && "".equals(rn.getPrefix()),
            "IS_NAMESPACE_AWARE=false: raw qname as localName, no ns");
        rn.close();
        // entity replacement off: ENTITY_REFERENCE events surfaced
        javax.xml.stream.XMLInputFactory fr = javax.xml.stream.XMLInputFactory.newFactory();
        fr.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        javax.xml.stream.XMLStreamReader rr = fr.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE r [<!ENTITY e \"RR\">]><r>&e;</r>"));
        rr.next(); // DTD
        rr.next(); // START r
        check(rr.next() == 9, "IS_REPLACING_ENTITY_REFERENCES=false -> ENTITY_REFERENCE(9)");
        check("e".equals(rr.getLocalName()) && "RR".equals(rr.getText()),
            "entity reference local name and replacement text");
        rr.close();
        // SUPPORT_DTD=false: entity undeclared -> XMLStreamException
        javax.xml.stream.XMLInputFactory fd = javax.xml.stream.XMLInputFactory.newFactory();
        fd.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        boolean dtdErr = false;
        javax.xml.stream.XMLStreamReader rd = fd.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE r [<!ENTITY e \"x\">]><r>&e;</r>"));
        try { while (rd.hasNext()) rd.next(); }
        catch (javax.xml.stream.XMLStreamException e) { dtdErr = true; }
        check(dtdErr, "SUPPORT_DTD=false + entity ref -> XMLStreamException");
        if (rd != null) { try { rd.close(); } catch (Throwable ignore) {} }
    }

    // ---- Deepened 4: createFilteredReader with both filter flavors ----
    static void testFilteredReaders() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        // StreamFilter: only element events pass
        javax.xml.stream.XMLStreamReader src = f.createXMLStreamReader(new java.io.StringReader(
            "<a>1<b>2</b>3<!--c--></a>"));
        javax.xml.stream.XMLStreamReader fr = f.createFilteredReader(src, new javax.xml.stream.StreamFilter() {
            public boolean accept(javax.xml.stream.XMLStreamReader r) {
                return r.isStartElement() || r.isEndElement();
            }
        });
        java.util.List<String> seen = new java.util.ArrayList<String>();
        while (fr.hasNext()) {
            int t = fr.next();
            if (t == 1) seen.add("SE" + fr.getLocalName());
            else if (t == 2) seen.add("EE" + fr.getLocalName());
            else seen.add("EV" + t);
        }
        fr.close();
        check(seen.equals(java.util.Arrays.asList("SEb", "EEb", "EEa", "EV8")),
            "StreamFilter drops text/comment; END_DOCUMENT still delivered, got " + seen);
        // StreamFilter accepting nothing: no events at all
        javax.xml.stream.XMLStreamReader src2 = f.createXMLStreamReader(new java.io.StringReader("<a><b/></a>"));
        javax.xml.stream.XMLStreamReader none = f.createFilteredReader(src2, new javax.xml.stream.StreamFilter() {
            public boolean accept(javax.xml.stream.XMLStreamReader r) { return false; }
        });
        int cnt = 0;
        while (none.hasNext()) { none.next(); cnt++; }
        none.close();
        check(cnt == 0, "reject-all StreamFilter yields zero events");
        // EventFilter: only start elements
        javax.xml.stream.XMLEventReader esrc = f.createXMLEventReader(new java.io.StringReader(
            "<a>1<b/></a>"));
        javax.xml.stream.XMLEventReader efr = f.createFilteredReader(esrc, new javax.xml.stream.EventFilter() {
            public boolean accept(javax.xml.stream.events.XMLEvent e) { return e.isStartElement(); }
        });
        java.util.List<String> names = new java.util.ArrayList<String>();
        while (efr.hasNext()) names.add(efr.nextEvent().asStartElement().getName().getLocalPart());
        efr.close();
        check(names.equals(java.util.Arrays.asList("a", "b")), "EventFilter start-only, got " + names);
    }

    // ---- Deepened 5: factory discovery identity ----
    static void testFactoryIdentity() throws Exception {
        javax.xml.stream.XMLInputFactory a = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLInputFactory b = javax.xml.stream.XMLInputFactory.newInstance();
        check(a != null && b != null && a != b, "newFactory/newFactory and newInstance return fresh instances");
        check(a.getClass() == b.getClass(), "same implementation class for both discovery methods");
        javax.xml.stream.XMLInputFactory d = javax.xml.stream.XMLInputFactory.newDefaultFactory();
        check(d != null && d != a, "JDK-27: newDefaultFactory() returns a working distinct instance (absent in JDK-8)");
        // system-property based lookup with finally-restore
        String key = "javax.xml.stream.XMLInputFactory";
        String saved = System.getProperty(key);
        try {
            System.setProperty(key, "com.sun.xml.internal.stream.XMLInputFactoryImpl");
            javax.xml.stream.XMLInputFactory c = javax.xml.stream.XMLInputFactory.newFactory();
            check(c != null, "factory via system property override resolves");
        } finally {
            if (saved == null) System.clearProperty(key); else System.setProperty(key, saved);
        }
        check(System.getProperty(key) == null ? saved == null : saved.equals(System.getProperty(key)),
            "system property restored");
    }
}
