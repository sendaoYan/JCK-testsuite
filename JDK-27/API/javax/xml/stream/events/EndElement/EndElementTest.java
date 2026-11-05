/*
 * @test
 * @summary javax.xml.stream.events.EndElement conformance: white-box structure + black-box behavior.
 * @run main EndElementTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EndElementTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.EndElement");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNamespaces"), "method getNamespaces");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.EndElement a = new javax.xml.stream.events.EndElement(){
            public javax.xml.namespace.QName getName() { return null; }
            public java.util.Iterator<javax.xml.stream.events.Namespace> getNamespaces() { return null; }
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
        check(a != null, "anon impl of EndElement");
        check(a.getName() == null, "getName() returns fixed default");
        check(a.getNamespaces() == null, "getNamespaces() returns fixed default");
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

    // ---- Part 2: contract deepening (parsed + factory EndElement) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) parsed EndElement: code 2, name exact, isXxx family, self cast, empty namespaces
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<p:r xmlns='http://d' xmlns:p='http://p'>x</p:r>"));
        r.nextEvent();
        r.nextEvent();
        r.nextEvent();
        javax.xml.stream.events.XMLEvent e = r.nextEvent();
        check(e.getEventType() == 2 && e.isEndElement() && !e.isStartElement() && !e.isCharacters(),
                "EE code 2 + family");
        javax.xml.stream.events.EndElement ee = e.asEndElement();
        check(ee == e, "asEndElement self");
        check("http://p".equals(ee.getName().getNamespaceURI()) && "r".equals(ee.getName().getLocalPart())
                && "p".equals(ee.getName().getPrefix()), "EE QName (http://p, r, p)");
        // JDK impl: parsed EndElement reports no out-of-scope namespaces even when declared at this element
        check(!ee.getNamespaces().hasNext(), "parsed EE namespaces empty (impl)");
        r.close();

        // 2) factory createEndElement(QName, nsIterator): name kept, namespace iterator swallowed by impl
        java.util.List<javax.xml.stream.events.Namespace> nl = new java.util.ArrayList<javax.xml.stream.events.Namespace>();
        nl.add(ef.createNamespace("p", "http://p"));
        javax.xml.stream.events.EndElement f1 =
                ef.createEndElement(new javax.xml.namespace.QName("http://d", "root", "p"), nl.iterator());
        check(f1.getEventType() == 2 && f1.isEndElement(), "factory EE code 2");
        check("http://d".equals(f1.getName().getNamespaceURI()) && "root".equals(f1.getName().getLocalPart())
                && "p".equals(f1.getName().getPrefix()), "factory EE QName");
        check(!f1.getNamespaces().hasNext(), "factory EE namespace iterator swallowed (impl)");

        // 3) factory (prefix, nsURI, local) and (+nsIterator) forms
        javax.xml.stream.events.EndElement f2 = ef.createEndElement("p", "http://p", "local");
        check(f2.getName().getPrefix().equals("p") && f2.getName().getLocalPart().equals("local")
                && f2.getName().getNamespaceURI().equals("http://p"), "factory EE 3-arg name");
        javax.xml.stream.events.EndElement f3 = ef.createEndElement("p", "http://p", "local", nl.iterator());
        check(f3.getName().equals(f2.getName()) && !f3.getNamespaces().hasNext(), "factory EE 4-arg name + empty ns");

        // 4) writeAsEncodedUnicode exact (prefix:local form, unlike the bracket start form)
        java.io.StringWriter w = new java.io.StringWriter();
        f2.writeAsEncodedUnicode(w);
        check("</p:local>".equals(w.toString()), "factory EE encoded </p:local>");

        // 5) EndElement as part of the reader flow: after inner elements, order preserved
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><c/></r>"));
        r2.nextEvent(); r2.nextEvent(); r2.nextEvent();
        check(r2.nextEvent().asEndElement().getName().getLocalPart().equals("c"), "inner EE name c");
        check(r2.nextEvent().asEndElement().getName().getLocalPart().equals("r"), "outer EE name r");
        check(r2.nextEvent().getEventType() == 8, "END_DOCUMENT after EE");
        r2.close();

        // 6) wrong casts
        try { f2.asCharacters(); check(false, "EE.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { f2.asStartElement(); check(false, "EE.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        check(f2.getSchemaType() == null, "EE getSchemaType null");
    }
}
