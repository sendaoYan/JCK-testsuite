/*
 * @test
 * @summary javax.xml.stream.events.StartElement conformance: white-box structure + black-box behavior.
 * @run main StartElementTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StartElementTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.StartElement");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getAttributeByName"), "method getAttributeByName");
        check(hasMethod(c, "getAttributes"), "method getAttributes");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getNamespaces"), "method getNamespaces");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.StartElement a = new javax.xml.stream.events.StartElement(){
            public javax.xml.namespace.QName getName() { return null; }
            public java.util.Iterator<javax.xml.stream.events.Attribute> getAttributes() { return null; }
            public java.util.Iterator<javax.xml.stream.events.Namespace> getNamespaces() { return null; }
            public javax.xml.stream.events.Attribute getAttributeByName(javax.xml.namespace.QName p0) { return null; }
            public javax.xml.namespace.NamespaceContext getNamespaceContext() { return null; }
            public java.lang.String getNamespaceURI(java.lang.String p0) { return "s"; }
            public int getEventType() { return 3; }
            public javax.xml.stream.Location getLocation() { return null; }
            public boolean isStartElement() { return true; }
            public boolean isAttribute() { return true; }
            public boolean isNamespace() { return true; }
            public boolean isEndElement() { return true; }
            public boolean isEntityReference() { return true; }
            public boolean isProcessingInstruction() { return true; }
            public boolean isCharacters() { return true; }
            public boolean isStartDocument() { return true; }
            public boolean isEndDocument() { return true; }
            public javax.xml.stream.events.StartElement asStartElement() { return null; }
            public javax.xml.stream.events.EndElement asEndElement() { return null; }
            public javax.xml.stream.events.Characters asCharacters() { return null; }
            public javax.xml.namespace.QName getSchemaType() { return null; }
            public void writeAsEncodedUnicode(java.io.Writer p0) { }
        };
        check(a != null, "anon impl of StartElement");
        check(a.getName() == null, "getName() returns fixed default");
        check(a.getAttributes() == null, "getAttributes() returns fixed default");
        check(a.getNamespaces() == null, "getNamespaces() returns fixed default");
        check(a.getAttributeByName((javax.xml.namespace.QName) null) == null, "getAttributeByName() returns fixed default");
        check(a.getNamespaceContext() == null, "getNamespaceContext() returns fixed default");
        check("s".equals(a.getNamespaceURI("s")), "getNamespaceURI() returns fixed default");
        check(a.getEventType() == 3, "getEventType() returns fixed default");
        check(a.getLocation() == null, "getLocation() returns fixed default");
        check(a.isStartElement() == true, "isStartElement() returns fixed default");
        check(a.isAttribute() == true, "isAttribute() returns fixed default");
        check(a.isNamespace() == true, "isNamespace() returns fixed default");
        check(a.isEndElement() == true, "isEndElement() returns fixed default");
        check(a.isEntityReference() == true, "isEntityReference() returns fixed default");
        check(a.isProcessingInstruction() == true, "isProcessingInstruction() returns fixed default");
        check(a.isCharacters() == true, "isCharacters() returns fixed default");
        check(a.isStartDocument() == true, "isStartDocument() returns fixed default");
        check(a.isEndDocument() == true, "isEndDocument() returns fixed default");
        check(a.asStartElement() == null, "asStartElement() returns fixed default");
        check(a.asEndElement() == null, "asEndElement() returns fixed default");
        check(a.asCharacters() == null, "asCharacters() returns fixed default");
        check(a.getSchemaType() == null, "getSchemaType() returns fixed default");
        a.writeAsEncodedUnicode((java.io.Writer) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (parsed + factory StartElement) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) unqualified attributes: exact document order a,b,c,d with value/type/specified
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1' b='2' c='3' d='4'/>"));
        r.nextEvent();
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        check(se.getName().getNamespaceURI().equals("") && se.getName().getLocalPart().equals("r")
                && se.getName().getPrefix().equals(""), "parsed plain name {''}r prefix ''");
        String[] want = {"a", "b", "c", "d"};
        String[] vals = {"1", "2", "3", "4"};
        int i = 0;
        for (java.util.Iterator<?> it = se.getAttributes(); it.hasNext(); ) {
            javax.xml.stream.events.Attribute at = (javax.xml.stream.events.Attribute) it.next();
            check(i < 4 && at.getName().getLocalPart().equals(want[i]) && at.getValue().equals(vals[i]),
                    "attr[" + i + "] " + want[i] + "=" + vals[i] + " in doc order");
            i++;
        }
        check(i == 4, "attribute count 4");
        javax.xml.stream.events.Attribute a0 = se.getAttributeByName(new javax.xml.namespace.QName("b"));
        check(a0 != null && "2".equals(a0.getValue()), "getAttributeByName(b)");
        check(se.getAttributeByName(new javax.xml.namespace.QName("zzz")) == null, "getAttributeByName miss null");
        r.close();

        // 2) empty iterators for attribute-less / namespace-less element
        javax.xml.stream.XMLEventReader r0 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        r0.nextEvent();
        javax.xml.stream.events.StartElement se0 = r0.nextEvent().asStartElement();
        check(!se0.getAttributes().hasNext() && !se0.getNamespaces().hasNext(), "empty attr/ns iterators");
        r0.close();

        // 3) namespaced element: QName ns/local/prefix exact + namespace lookup + context
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<p:r xmlns='http://d' xmlns:p='http://p' a='1' p:b='2' c='3'/>"));
        r2.nextEvent();
        javax.xml.stream.events.StartElement sn = r2.nextEvent().asStartElement();
        check("http://p".equals(sn.getName().getNamespaceURI()) && "r".equals(sn.getName().getLocalPart())
                && "p".equals(sn.getName().getPrefix()), "parsed QName (http://p, r, p)");
        check("http://p".equals(sn.getNamespaceURI("p")), "getNamespaceURI(p)");
        check("http://d".equals(sn.getNamespaceURI("")), "getNamespaceURI('') default");
        check(sn.getNamespaceURI("nosuch") == null, "getNamespaceURI unknown null");
        javax.xml.namespace.NamespaceContext nc = sn.getNamespaceContext();
        check("http://d".equals(nc.getNamespaceURI("")) && "http://p".equals(nc.getNamespaceURI("p")),
                "context URI('')/URI(p)");
        check("p".equals(nc.getPrefix("http://p")) && "".equals(nc.getPrefix("http://d")),
                "context prefix lookups");
        int pc = 0;
        for (java.util.Iterator<?> pi = nc.getPrefixes("http://p"); pi.hasNext(); ) { pc++; pi.next(); }
        check(pc == 1, "prefixes(http://p) count 1");
        try { nc.getNamespaceURI(null); check(false, "context URI(null) must IAE"); }
        catch (IllegalArgumentException iae) { passed++; }
        // mixed attrs: unqualified in doc order first, then qualified (deterministic impl order a,c,b)
        String[] mixOrder = {"a", "c", "b"};
        java.util.Set<String> names = new java.util.HashSet<String>();
        int ac = 0;
        for (java.util.Iterator<?> it = sn.getAttributes(); it.hasNext(); ) {
            javax.xml.stream.events.Attribute at = (javax.xml.stream.events.Attribute) it.next();
            check(at.getName().getLocalPart().equals(mixOrder[ac]), "mixed attr order[" + ac + "]=" + mixOrder[ac]);
            names.add(at.getName().getLocalPart());
            ac++;
        }
        check(ac == 3 && names.contains("a") && names.contains("b") && names.contains("c"), "mixed attrs membership");
        javax.xml.stream.events.Attribute pb = sn.getAttributeByName(new javax.xml.namespace.QName("http://p", "b"));
        check(pb != null && "2".equals(pb.getValue()) && "p".equals(pb.getName().getPrefix()), "getAttributeByName(ns,b)");
        // namespace declarations via getNamespaces: default + prefixed entries
        int nd = 0; int defCount = 0, prefCount = 0;
        for (java.util.Iterator<?> it = sn.getNamespaces(); it.hasNext(); ) {
            javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace) it.next();
            if (ns.isDefaultNamespaceDeclaration()) {
                defCount++;
                check("".equals(ns.getPrefix()) && "http://d".equals(ns.getNamespaceURI()), "default ns entry");
            } else {
                prefCount++;
                check("p".equals(ns.getPrefix()) && "http://p".equals(ns.getNamespaceURI()), "prefixed ns entry");
            }
            check("CDATA".equals(ns.getDTDType()) && !ns.isSpecified() && !ns.isAttribute(), "ns as Attribute contract");
            nd++;
        }
        check(nd == 2 && defCount == 1 && prefCount == 1, "two namespace declarations");
        // writeAsEncodedUnicode exact for parsed element (impl bracket QName + xmlns:= default-ns quirk)
        java.io.StringWriter w = new java.io.StringWriter();
        sn.writeAsEncodedUnicode(w);
        check(w.toString().equals("<['http://p']:p:r a='1' c='3' p:b='2' xmlns:='http://d' xmlns:p='http://p'>"),
                "parsed prefixed SE encoded exact");
        r2.close();

        // 4) getAttributes() iterator does not support remove
        javax.xml.stream.XMLEventReader r4 = ifac.createXMLEventReader(new java.io.StringReader("<r a='1'/>"));
        r4.nextEvent();
        java.util.Iterator<?> itr = r4.nextEvent().asStartElement().getAttributes();
        try { itr.remove(); check(false, "attr iterator remove must throw"); }
        catch (UnsupportedOperationException uoe) { passed++; }
        catch (IllegalStateException ise) { passed++; }
        r4.close();

        // 5) undeclared attribute prefix -> XMLStreamException (AttributePrefixUnbound)
        try {
            javax.xml.stream.XMLEventReader r5 = ifac.createXMLEventReader(
                    new java.io.StringReader("<r p:b='2'/>"));
            while (r5.hasNext()) r5.nextEvent();
            check(false, "undeclared attr prefix must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("AttributePrefixUnbound"), "AttributePrefixUnbound message");
        }

        // 6) factory createStartElement: attrs/namespaces in insertion order (unqualified), lookups
        java.util.List<javax.xml.stream.events.Attribute> al = new java.util.ArrayList<javax.xml.stream.events.Attribute>();
        al.add(ef.createAttribute("a", "1"));
        al.add(ef.createAttribute("b", "2"));
        al.add(ef.createAttribute("c", "3"));
        java.util.List<javax.xml.stream.events.Namespace> nl = new java.util.ArrayList<javax.xml.stream.events.Namespace>();
        nl.add(ef.createNamespace("x", "http://x"));
        nl.add(ef.createNamespace("http://def"));
        javax.xml.stream.events.StartElement fs =
                ef.createStartElement(new javax.xml.namespace.QName("http://t", "root", "t"), al.iterator(), nl.iterator());
        check("http://t".equals(fs.getName().getNamespaceURI()) && "root".equals(fs.getName().getLocalPart())
                && "t".equals(fs.getName().getPrefix()), "factory SE QName");
        int j = 0;
        String[] fw = {"a", "b", "c"};
        for (java.util.Iterator<?> it = fs.getAttributes(); it.hasNext(); ) {
            javax.xml.stream.events.Attribute at = (javax.xml.stream.events.Attribute) it.next();
            check(at.getName().getLocalPart().equals(fw[j]) && !at.isSpecified() && "CDATA".equals(at.getDTDType()),
                    "factory attr[" + j + "] order + isSpecified false");
            j++;
        }
        check(j == 3, "factory attr count");
        String[] fp = {"x", ""};
        String[] fu = {"http://x", "http://def"};
        int m = 0;
        for (java.util.Iterator<?> it = fs.getNamespaces(); it.hasNext(); ) {
            javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace) it.next();
            check(ns.getPrefix().equals(fp[m]) && ns.getNamespaceURI().equals(fu[m]), "factory ns[" + m + "] order");
            m++;
        }
        check(m == 2, "factory ns count");
        // JDK impl quirk: factory StartElement does not wire namespaces into lookup/context
        check(fs.getNamespaceURI("x") == null, "factory getNamespaceURI(x) null (impl)");
        check(fs.getNamespaceContext() == null, "factory getNamespaceContext null (impl)");
        check("1".equals(fs.getAttributeByName(new javax.xml.namespace.QName("a")).getValue()), "factory getAttributeByName");
        check(fs.getAttributeByName(new javax.xml.namespace.QName("zz")) == null, "factory getAttributeByName miss");
        // prefix-form encoded output (impl bracket form)
        java.io.StringWriter w2 = new java.io.StringWriter();
        ef.createStartElement("p", "http://p", "local").writeAsEncodedUnicode(w2);
        check(w2.toString().equals("<['http://p']:p:local>"), "factory prefix-form encoded bracket quirk");
    }
}
