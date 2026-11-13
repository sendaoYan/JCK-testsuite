/*
 * @test
 * @summary javax.xml.stream.events.EntityReference conformance: white-box structure + black-box behavior.
 * @run main EntityReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityReferenceTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.EntityReference");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getDeclaration"), "method getDeclaration");
        check(hasMethod(c, "getName"), "method getName");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.EntityReference a = new javax.xml.stream.events.EntityReference(){
            public javax.xml.stream.events.EntityDeclaration getDeclaration() { return null; }
            public java.lang.String getName() { return "s"; }
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
        check(a != null, "anon impl of EntityReference");
        check(a.getDeclaration() == null, "getDeclaration() returns fixed default");
        check("s".equals(a.getName()), "getName() returns fixed default");
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
        // 固定默认区域为英文：StAX 解析器的未解析实体错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        Locale.setDefault(Locale.ENGLISH);
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory + parsed with replacing on/off) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createEntityReference: name kept, null declaration tolerated, code 9
        javax.xml.stream.events.EntityReference er = ef.createEntityReference("foo", null);
        check("foo".equals(er.getName()), "factory ER name");
        check(er.getDeclaration() == null, "factory ER declaration null");
        check(er.getEventType() == 9 && er.isEntityReference(), "factory ER code 9 + family");
        check(!er.isCharacters() && !er.isStartElement() && !er.isEndElement(), "factory ER isXxx rest false");
        java.io.StringWriter w = new java.io.StringWriter();
        er.writeAsEncodedUnicode(w);
        check("&foo;".equals(w.toString()), "factory ER encoded &foo;");
        javax.xml.stream.events.EntityReference ern = ef.createEntityReference(null, null);
        check(ern.getName() == null, "createEntityReference(null,null) tolerated");

        // 2) parsed with IS_REPLACING_ENTITY_REFERENCES=false: internal entity yields ER event
        javax.xml.stream.XMLInputFactory nf = javax.xml.stream.XMLInputFactory.newFactory();
        nf.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        javax.xml.stream.XMLEventReader r = nf.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r [<!ENTITY e1 'Hello'>]><r>&e1;</r>"));
        boolean saw = false;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e.isEntityReference()) {
                saw = true;
                javax.xml.stream.events.EntityReference per = (javax.xml.stream.events.EntityReference) e;
                check("e1".equals(per.getName()), "parsed ER name e1");
                javax.xml.stream.events.EntityDeclaration d = per.getDeclaration();
                check(d != null && "e1".equals(d.getName()) && "Hello".equals(d.getReplacementText()),
                        "parsed ER declaration wired to internal entity");
                check(e.getEventType() == 9, "parsed ER code 9");
            }
        }
        check(saw, "ENTITY_REFERENCE event observed with replacing=false");
        r.close();

        // 3) with replacing=true (default): no ER event, replacement characters instead
        javax.xml.stream.XMLEventReader r2 = javax.xml.stream.XMLInputFactory.newFactory()
                .createXMLEventReader(new java.io.StringReader("<!DOCTYPE r [<!ENTITY e1 'Hello'>]><r>&e1;</r>"));
        boolean saw2 = false; String text = "";
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e.isEntityReference()) saw2 = true;
            if (e.isCharacters()) text = e.asCharacters().getData();
        }
        check(!saw2 && "Hello".equals(text), "replacing=true replaces entity with characters");
        r2.close();

        // 4) unparsed entity (NDATA) in content -> XMLStreamException
        try {
            javax.xml.stream.XMLEventReader r3 = javax.xml.stream.XMLInputFactory.newFactory()
                    .createXMLEventReader(new java.io.StringReader(
                            "<!DOCTYPE r [<!ENTITY e2 SYSTEM 'x.bin' NDATA gif><!NOTATION gif SYSTEM 'fmt'>]><r>&e2;</r>"));
            while (r3.hasNext()) r3.nextEvent();
            check(false, "unparsed entity reference must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("not permitted"), "unparsed entity message");
        }
    }
}
