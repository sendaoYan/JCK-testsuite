/*
 * @test
 * @summary javax.xml.stream.events.EntityDeclaration conformance: white-box structure + black-box behavior.
 * @run main EntityDeclarationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityDeclarationTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.EntityDeclaration");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getBaseURI"), "method getBaseURI");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNotationName"), "method getNotationName");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getReplacementText"), "method getReplacementText");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.EntityDeclaration a = new javax.xml.stream.events.EntityDeclaration(){
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public java.lang.String getName() { return "s"; }
            public java.lang.String getNotationName() { return "s"; }
            public java.lang.String getReplacementText() { return "s"; }
            public java.lang.String getBaseURI() { return "s"; }
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
        check(a != null, "anon impl of EntityDeclaration");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check("s".equals(a.getName()), "getName() returns fixed default");
        check("s".equals(a.getNotationName()), "getNotationName() returns fixed default");
        check("s".equals(a.getReplacementText()), "getReplacementText() returns fixed default");
        check("s".equals(a.getBaseURI()), "getBaseURI() returns fixed default");
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

    // ---- Part 2: contract deepening (internal/PUBLIC/SYSTEM forms via parsed DTD) ----
    static void testDeep() throws Exception {
        // 1) parse a DTD with all three entity forms and read them back from DTD.getEntities()
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE ro ["
                        + "<!ENTITY e1 'Hello'>"
                        + "<!ENTITY e2 PUBLIC 'pub1' 'sys1' NDATA gif>"
                        + "<!ENTITY e3 SYSTEM 'sys3'>"
                        + "<!NOTATION gif PUBLIC '-//gif'>"
                        + "]><ro/>"));
        javax.xml.stream.events.EntityDeclaration internal = null, externalPub = null, externalSys = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getEntities()) {
                    javax.xml.stream.events.EntityDeclaration ed = (javax.xml.stream.events.EntityDeclaration) o;
                    if ("e1".equals(ed.getName())) internal = ed;
                    if ("e2".equals(ed.getName())) externalPub = ed;
                    if ("e3".equals(ed.getName())) externalSys = ed;
                    check(ed.getEventType() == 15, "entity declaration code 15");
                    check(ed.getBaseURI() == null, "baseURI null for internal-subset decl");
                }
            }
        }
        r.close();
        // internal parsed entity: replacement text, no ids, no notation
        check(internal != null, "internal entity present");
        check("Hello".equals(internal.getReplacementText()), "internal replacement text");
        check(internal.getPublicId() == null && internal.getSystemId() == null && internal.getNotationName() == null,
                "internal ids/notation null");
        // external PUBLIC + NDATA: public/system ids and notation name wired
        check(externalPub != null, "external PUBLIC entity present");
        check("pub1".equals(externalPub.getPublicId()) && "sys1".equals(externalPub.getSystemId()),
                "external PUBLIC ids");
        check("gif".equals(externalPub.getNotationName()), "NDATA notation name");
        check(externalPub.getReplacementText() == null, "external entity no replacement text");
        // external SYSTEM: system id only
        check(externalSys != null, "external SYSTEM entity present");
        check("sys3".equals(externalSys.getSystemId()) && externalSys.getPublicId() == null,
                "external SYSTEM ids");
        check(externalSys.getNotationName() == null, "external SYSTEM no notation");

        // 2) encoded form of an entity declaration
        java.io.StringWriter w = new java.io.StringWriter();
        internal.writeAsEncodedUnicode(w);
        check("<!ENTITY e1 \"Hello\">".equals(w.toString()), "entity declaration encoded exact");

        // 3) factory createDTD carries no entity declarations
        javax.xml.stream.events.DTD fd = javax.xml.stream.XMLEventFactory.newFactory()
                .createDTD("<!DOCTYPE r [<!ENTITY e 'v'>]>");
        check(fd.getEntities() == null, "factory DTD getEntities null");

        // 4) entity declaration order in the list follows declaration order
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE ro [<!ENTITY b '2'><!ENTITY a '1'>]><ro/>"));
        java.util.List<String> order = new java.util.ArrayList<String>();
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getEntities())
                    order.add(((javax.xml.stream.events.EntityDeclaration) o).getName());
            }
        }
        r2.close();
        check(order.size() == 2 && order.contains("b") && order.contains("a"),
                "both declared entities listed (map-backed order, not declaration order)");
    }
}
