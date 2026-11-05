/*
 * @test
 * @summary javax.xml.stream.events.DTD conformance: white-box structure + black-box behavior.
 * @run main DTDTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DTDTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.DTD");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getDocumentTypeDeclaration"), "method getDocumentTypeDeclaration");
        check(hasMethod(c, "getEntities"), "method getEntities");
        check(hasMethod(c, "getNotations"), "method getNotations");
        check(hasMethod(c, "getProcessedDTD"), "method getProcessedDTD");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.DTD a = new javax.xml.stream.events.DTD(){
            public java.lang.String getDocumentTypeDeclaration() { return "s"; }
            public java.lang.Object getProcessedDTD() { return null; }
            public java.util.List<javax.xml.stream.events.NotationDeclaration> getNotations() { return null; }
            public java.util.List<javax.xml.stream.events.EntityDeclaration> getEntities() { return null; }
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
        check(a != null, "anon impl of DTD");
        check("s".equals(a.getDocumentTypeDeclaration()), "getDocumentTypeDeclaration() returns fixed default");
        check(a.getProcessedDTD() == null, "getProcessedDTD() returns fixed default");
        check(a.getNotations() == null, "getNotations() returns fixed default");
        check(a.getEntities() == null, "getEntities() returns fixed default");
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

    // ---- Part 2: contract deepening (full/empty/absent DTD matrix + factory) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) full internal subset: exact declaration string, entities and notations lists
        String decl = "<!DOCTYPE root [\n"
                + "<!ENTITY e1 'Hello'>\n"
                + "<!ENTITY e2 SYSTEM 'ent.bin' NDATA bin>\n"
                + "<!NOTATION bin SYSTEM 'binfmt'>\n"
                + "<!NOTATION gif PUBLIC '-//gif//gif'>\n"
                + "]>";
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader(decl + "\n<root/>"));
        javax.xml.stream.events.DTD dtd = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) dtd = (javax.xml.stream.events.DTD) e;
        }
        r.close();
        check(dtd != null, "DTD event present");
        check(dtd.getEventType() == 11, "DTD code 11");
        check(decl.equals(dtd.getDocumentTypeDeclaration()), "DTD declaration string exact");
        check(dtd.getEntities() != null && dtd.getEntities().size() == 2, "two entities");
        check("e1".equals(((javax.xml.stream.events.EntityDeclaration) dtd.getEntities().get(0)).getName())
                && "e2".equals(((javax.xml.stream.events.EntityDeclaration) dtd.getEntities().get(1)).getName()),
                "entity names in order");
        check(dtd.getNotations() != null && dtd.getNotations().size() == 2, "two notations");
        check("bin".equals(((javax.xml.stream.events.NotationDeclaration) dtd.getNotations().get(0)).getName())
                && "gif".equals(((javax.xml.stream.events.NotationDeclaration) dtd.getNotations().get(1)).getName()),
                "notation names in order");
        check(dtd.getProcessedDTD() == null, "processed DTD null");
        check(!dtd.isCharacters() && !dtd.isStartElement(), "DTD isXxx family");

        // 2) empty internal subset: null entity/notation lists
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r ><r/>"));
        javax.xml.stream.events.DTD dtd2 = null;
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) dtd2 = (javax.xml.stream.events.DTD) e;
        }
        r2.close();
        check(dtd2 != null, "empty-subset DTD event present");
        check("<!DOCTYPE r >".equals(dtd2.getDocumentTypeDeclaration()), "empty subset declaration exact");
        check(dtd2.getEntities() == null && dtd2.getNotations() == null, "empty subset null lists");

        // 3) no DOCTYPE at all: no DTD event
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        boolean saw = false;
        while (r3.hasNext()) { if (r3.nextEvent().getEventType() == 11) saw = true; }
        r3.close();
        check(!saw, "no DTD event without DOCTYPE");

        // 4) factory createDTD: string round-trip, null lists, code, encoded
        javax.xml.stream.events.DTD fd = ef.createDTD(decl);
        check(decl.equals(fd.getDocumentTypeDeclaration()), "factory DTD string round-trip");
        check(fd.getEntities() == null && fd.getNotations() == null && fd.getProcessedDTD() == null,
                "factory DTD null lists (declarations not parsed)");
        check(fd.getEventType() == 11, "factory DTD code 11");
        java.io.StringWriter w = new java.io.StringWriter();
        fd.writeAsEncodedUnicode(w);
        check(decl.equals(w.toString()), "factory DTD encoded exact");
        java.io.StringWriter w2 = new java.io.StringWriter();
        ef.createDTD("<!DOCTYPE r>").writeAsEncodedUnicode(w2);
        check("<!DOCTYPE r>".equals(w2.toString()), "factory DTD minimal encoded");
        try { fd.asCharacters(); check(false, "DTD.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
    }
}
