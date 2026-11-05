/*
 * @test
 * @summary javax.xml.stream.events.Namespace conformance: white-box structure + black-box behavior.
 * @run main NamespaceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NamespaceTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.Namespace");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.Attribute"), "interface javax.xml.stream.events.Attribute");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "isDefaultNamespaceDeclaration"), "method isDefaultNamespaceDeclaration");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.Namespace a = new javax.xml.stream.events.Namespace(){
            public java.lang.String getPrefix() { return "s"; }
            public java.lang.String getNamespaceURI() { return "s"; }
            public boolean isDefaultNamespaceDeclaration() { return true; }
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
        check(a != null, "anon impl of Namespace");
        check("s".equals(a.getPrefix()), "getPrefix() returns fixed default");
        check("s".equals(a.getNamespaceURI()), "getNamespaceURI() returns fixed default");
        check(a.isDefaultNamespaceDeclaration() == true, "isDefaultNamespaceDeclaration() returns fixed default");
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

    // ---- Part 2: contract deepening (default vs prefixed matrix, factory + parsed) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory default-namespace declaration: createNamespace(uri)
        javax.xml.stream.events.Namespace def = ef.createNamespace("http://d");
        check("".equals(def.getPrefix()), "default ns prefix empty");
        check("http://d".equals(def.getNamespaceURI()), "default ns URI");
        check(def.isDefaultNamespaceDeclaration(), "default ns isDefault true");
        check(def.getName().getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")
                && def.getName().getLocalPart().equals(""), "default ns QName {xmlns-ns}''");
        check("http://d".equals(def.getValue()), "default ns getValue = URI");
        check("CDATA".equals(def.getDTDType()) && !def.isSpecified(), "default ns DTDType CDATA, not specified");
        check(def.isNamespace() && !def.isAttribute(), "isNamespace true, isAttribute false");
        check(def.getEventType() == 13, "namespace event code 13");

        // 2) factory prefixed declaration: createNamespace(prefix, uri)
        javax.xml.stream.events.Namespace pref = ef.createNamespace("p", "http://p");
        check("p".equals(pref.getPrefix()) && "http://p".equals(pref.getNamespaceURI()), "prefixed prefix/URI");
        check(!pref.isDefaultNamespaceDeclaration(), "prefixed isDefault false");
        check(pref.getName().getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")
                && pref.getName().getLocalPart().equals("p"), "prefixed ns QName {xmlns-ns}p");
        check("http://p".equals(pref.getValue()), "prefixed getValue = URI");
        // empty prefix explicitly -> default declaration
        javax.xml.stream.events.Namespace emp = ef.createNamespace("", "http://x");
        check("".equals(emp.getPrefix()) && emp.isDefaultNamespaceDeclaration(), "empty prefix -> default decl");
        // single-arg null tolerated, empty prefix
        javax.xml.stream.events.Namespace nul = ef.createNamespace((String) null);
        check("".equals(nul.getPrefix()) && nul.isDefaultNamespaceDeclaration(), "createNamespace(null) default decl");
        // two-arg null prefix -> IllegalArgumentException
        try { ef.createNamespace(null, "http://x"); check(false, "createNamespace(null,uri) must IAE"); }
        catch (IllegalArgumentException iae) { passed++; }

        // 3) parsed namespace declarations from a namespaced element: both entries exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r xmlns='http://d' xmlns:p='http://p'/>"));
        r.nextEvent();
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        int n = 0;
        for (java.util.Iterator<?> it = se.getNamespaces(); it.hasNext(); ) {
            javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace) it.next();
            if (ns.isDefaultNamespaceDeclaration()) {
                check("".equals(ns.getPrefix()) && "http://d".equals(ns.getNamespaceURI())
                        && "".equals(ns.getName().getLocalPart()), "parsed default ns entry exact");
            } else {
                check("p".equals(ns.getPrefix()) && "http://p".equals(ns.getNamespaceURI())
                        && "p".equals(ns.getName().getLocalPart()), "parsed prefixed ns entry exact");
            }
            check(ns.getEventType() == 13 && !ns.isAttribute(), "parsed ns code 13, not attribute");
            check("CDATA".equals(ns.getDTDType()) && !ns.isSpecified(), "parsed ns CDATA/unspecified");
            n++;
        }
        check(n == 2, "two parsed ns declarations");
        check("http://p".equals(se.getNamespaceURI("p")), "parent StartElement lookup by prefix");
        r.close();

        // 4) encoded form of namespace (as attribute-like xmlns writing)
        java.io.StringWriter w = new java.io.StringWriter();
        javax.xml.stream.events.StartElement sef = ef.createStartElement("", "", "r",
                java.util.Collections.<javax.xml.stream.events.Attribute>emptyIterator(),
                java.util.Collections.singletonList(pref).iterator());
        sef.writeAsEncodedUnicode(w);
        check(w.toString().contains("xmlns:p='http://p'"), "encoded SE contains xmlns:p declaration");
    }
}
