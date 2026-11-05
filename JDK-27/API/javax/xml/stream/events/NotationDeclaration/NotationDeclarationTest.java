/*
 * @test
 * @summary javax.xml.stream.events.NotationDeclaration conformance: white-box structure + black-box behavior.
 * @run main NotationDeclarationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NotationDeclarationTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.NotationDeclaration");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.NotationDeclaration a = new javax.xml.stream.events.NotationDeclaration(){
            public java.lang.String getName() { return "s"; }
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
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
        check(a != null, "anon impl of NotationDeclaration");
        check("s".equals(a.getName()), "getName() returns fixed default");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
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

    // ---- Part 2: contract deepening (PUBLIC/SYSTEM forms via parsed DTD) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();

        // 1) parse internal subset with PUBLIC and SYSTEM notations
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r ["
                        + "<!NOTATION gif PUBLIC '-//gif//gif'>"
                        + "<!NOTATION bin SYSTEM 'binfmt'>"
                        + "]><r/>"));
        javax.xml.stream.events.NotationDeclaration pub = null, sys = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getNotations()) {
                    javax.xml.stream.events.NotationDeclaration nd = (javax.xml.stream.events.NotationDeclaration) o;
                    if ("gif".equals(nd.getName())) pub = nd;
                    if ("bin".equals(nd.getName())) sys = nd;
                    check(nd.getEventType() == 14, "notation declaration code 14");
                }
            }
        }
        r.close();
        check(pub != null, "PUBLIC notation present");
        check("-//gif//gif".equals(pub.getPublicId()) && pub.getSystemId() == null, "PUBLIC notation ids");
        check(sys != null, "SYSTEM notation present");
        check("binfmt".equals(sys.getSystemId()) && sys.getPublicId() == null, "SYSTEM notation ids");

        // 2) encoded form exact
        java.io.StringWriter w = new java.io.StringWriter();
        pub.writeAsEncodedUnicode(w);
        check("<!NOTATION gif PUBLIC \"-//gif//gif\">".equals(w.toString()), "notation encoded exact");

        // 3) notation declarations are only reachable via the DTD event, never standalone
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r [<!NOTATION n SYSTEM 's'>]><r/>"));
        boolean standalone = false;
        while (r2.hasNext()) { if (r2.nextEvent().getEventType() == 14) standalone = true; }
        r2.close();
        check(!standalone, "no standalone NOTATION_DECLARATION event");

        // 4) doc without notations: empty/null list
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r [<!ENTITY e 'v'>]><r/>"));
        javax.xml.stream.events.DTD d = null;
        while (r3.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r3.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) d = (javax.xml.stream.events.DTD) e;
        }
        r3.close();
        check(d != null && d.getNotations() == null && d.getEntities() != null && d.getEntities().size() == 1,
                "entities-without-notations matrix");
    }
}
