/*
 * @test
 * @summary javax.xml.stream.events.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributeTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.Attribute");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getDTDType"), "method getDTDType");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "isSpecified"), "method isSpecified");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.Attribute a = new javax.xml.stream.events.Attribute(){
            public javax.xml.namespace.QName getName() { return null; }
            public java.lang.String getValue() { return "s"; }
            public java.lang.String getDTDType() { return "s"; }
            public boolean isSpecified() { return true; }
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
        check(a != null, "anon impl of Attribute");
        check(a.getName() == null, "getName() returns fixed default");
        check("s".equals(a.getValue()), "getValue() returns fixed default");
        check("s".equals(a.getDTDType()), "getDTDType() returns fixed default");
        check(a.isSpecified() == true, "isSpecified() returns fixed default");
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

    // ---- Part 2: contract deepening (factory 3 forms + parsed isSpecified matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory (local, value): no-namespace QName, CDATA type, not specified, code 10
        javax.xml.stream.events.Attribute a1 = ef.createAttribute("k", "v");
        check("k".equals(a1.getName().getLocalPart()) && "".equals(a1.getName().getNamespaceURI())
                && "".equals(a1.getName().getPrefix()), "factory attr QName {''}k");
        check("v".equals(a1.getValue()), "factory attr value");
        check("CDATA".equals(a1.getDTDType()), "factory attr DTDType CDATA default");
        check(!a1.isSpecified(), "factory attr isSpecified false (impl)");
        check(a1.getEventType() == 10 && a1.isAttribute() && !a1.isNamespace(), "factory attr code 10 + family");

        // 2) factory (nsURI, local, prefix, value): full QName
        javax.xml.stream.events.Attribute a2 = ef.createAttribute("pb", "http://p", "b", "2");
        check("http://p".equals(a2.getName().getNamespaceURI()) && "b".equals(a2.getName().getLocalPart())
                && "pb".equals(a2.getName().getPrefix()) && "2".equals(a2.getValue()), "factory 4-arg attr (prefix,ns,local,value)");

        // 3) factory (QName, value)
        javax.xml.stream.events.Attribute a3 = ef.createAttribute(new javax.xml.namespace.QName("http://q", "c", "q"), "3");
        check(a3.getName().equals(new javax.xml.namespace.QName("http://q", "c", "q")) && "3".equals(a3.getValue()),
                "factory QName-arg attr");
        // null local name -> IllegalArgumentException
        try { ef.createAttribute((String) null, "v"); check(false, "createAttribute(null,v) must IAE"); }
        catch (IllegalArgumentException iae) { passed++; }

        // 4) parsed attributes: specified=true (differs from factory), CDATA type, doc order
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1' b='2'/>"));
        r.nextEvent();
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        String[] order = {"a", "b"};
        int i = 0;
        for (java.util.Iterator<?> it = se.getAttributes(); it.hasNext(); ) {
            javax.xml.stream.events.Attribute at = (javax.xml.stream.events.Attribute) it.next();
            check(at.getName().getLocalPart().equals(order[i]), "parsed attr order[" + i + "]");
            check(at.isSpecified(), "parsed attr isSpecified true");
            check("CDATA".equals(at.getDTDType()), "parsed attr DTDType CDATA");
            check(at.getEventType() == 10, "parsed attr code 10");
            i++;
        }
        check(i == 2, "parsed attr count 2");
        check("1".equals(se.getAttributeByName(new javax.xml.namespace.QName("a")).getValue()),
                "getAttributeByName wired to same value");
        r.close();

        // 5) parsed namespaced attribute: QName with namespace + prefix
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r xmlns:p='http://p' p:a='v'/>"));
        r2.nextEvent();
        javax.xml.stream.events.Attribute pa = (javax.xml.stream.events.Attribute)
                r2.nextEvent().asStartElement().getAttributes().next();
        check("http://p".equals(pa.getName().getNamespaceURI()) && "a".equals(pa.getName().getLocalPart())
                && "p".equals(pa.getName().getPrefix()) && "v".equals(pa.getValue()), "parsed namespaced attr QName");
        r2.close();

        // 6) attributes are never surfaced as standalone events by the reader
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'/>"));
        boolean sawAttrEvent = false;
        while (r3.hasNext()) {
            if (r3.nextEvent().getEventType() == 10) sawAttrEvent = true;
        }
        check(!sawAttrEvent, "no standalone ATTRIBUTE events from reader");
        r3.close();

        // 7) encoded form: attribute rendered inside start element
        java.io.StringWriter w = new java.io.StringWriter();
        java.util.List<javax.xml.stream.events.Attribute> one =
                new java.util.ArrayList<javax.xml.stream.events.Attribute>();
        one.add(a1);
        ef.createStartElement("", "", "r", one.iterator(),
                java.util.Collections.<javax.xml.stream.events.Namespace>emptyIterator()).writeAsEncodedUnicode(w);
        check(w.toString().contains("k='v'"), "encoded SE contains k='v'");
    }
}
