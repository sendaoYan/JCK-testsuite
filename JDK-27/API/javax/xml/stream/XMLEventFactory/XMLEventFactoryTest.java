/*
 * @test
 * @summary javax.xml.stream.XMLEventFactory conformance: white-box structure + black-box behavior.
 * @run main XMLEventFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLEventFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "createAttribute"), "method createAttribute");
        check(hasMethod(c, "createCData"), "method createCData");
        check(hasMethod(c, "createCharacters"), "method createCharacters");
        check(hasMethod(c, "createComment"), "method createComment");
        check(hasMethod(c, "createDTD"), "method createDTD");
        check(hasMethod(c, "createEndDocument"), "method createEndDocument");
        check(hasMethod(c, "createEndElement"), "method createEndElement");
        check(hasMethod(c, "createEntityReference"), "method createEntityReference");
        check(hasMethod(c, "createIgnorableSpace"), "method createIgnorableSpace");
        check(hasMethod(c, "createNamespace"), "method createNamespace");
        check(hasMethod(c, "createProcessingInstruction"), "method createProcessingInstruction");
        check(hasMethod(c, "createSpace"), "method createSpace");
        check(hasMethod(c, "createStartDocument"), "method createStartDocument");
        check(hasMethod(c, "createStartElement"), "method createStartElement");
        check(hasMethod(c, "newDefaultFactory"), "method newDefaultFactory");
        check(hasMethod(c, "newFactory"), "method newFactory");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "setLocation"), "method setLocation");

        javax.xml.stream.XMLEventFactory f = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.events.StartDocument sd = f.createStartDocument();
        check(sd.isStartDocument(), "startDocument");
        javax.xml.stream.events.StartElement se = f.createStartElement("", "", "root");
        check("root".equals(se.getName().getLocalPart()), "startElement");
        javax.xml.stream.events.EndElement ee = f.createEndElement("", "", "root");
        check("root".equals(ee.getName().getLocalPart()), "endElement");
        check("hello".equals(f.createCharacters("hello").getData()), "characters");
        check("c".equals(f.createComment("c").getText()), "comment");
        check(f.createEndDocument().isEndDocument(), "endDocument");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventFactory f = javax.xml.stream.XMLEventFactory.newFactory();
        check(f != null, "newFactory");
        javax.xml.stream.events.StartElement se = f.createStartElement("p", "http://ns", "local");
        check(se.getName().getLocalPart().equals("local"), "createStartElement local");
        check("http://ns".equals(se.getName().getNamespaceURI()), "createStartElement ns");
        javax.xml.stream.events.EndElement ee = f.createEndElement("p", "http://ns", "local");
        check("local".equals(ee.getName().getLocalPart()), "createEndElement");
        javax.xml.stream.events.Attribute at = f.createAttribute("k", "v");
        check("k".equals(at.getName().getLocalPart()) && "v".equals(at.getValue()), "createAttribute");
        javax.xml.stream.events.Characters ch = f.createCharacters("txt");
        check(ch.isCharacters() && "txt".equals(ch.getData()), "createCharacters");
        javax.xml.stream.events.StartDocument sd = f.createStartDocument();
        check(sd.isStartDocument(), "createStartDocument");
        javax.xml.stream.events.Comment cm = f.createComment("c");
        check("c".equals(cm.getText()), "createComment");
        javax.xml.stream.events.Namespace ns = f.createNamespace("p", "u");
        check("p".equals(ns.getPrefix()) && "u".equals(ns.getNamespaceURI()), "createNamespace");
        check(javax.xml.stream.XMLEventFactory.newInstance() != null, "newInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCreateFamily();
        testEventTypesAndFlags();
        testWriteOutClosure();
        testFactoryIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: full create family incl QName+Iterator forms ----
    static void testCreateFamily() throws Exception {
        javax.xml.stream.XMLEventFactory f = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.namespace.QName qn = new javax.xml.namespace.QName("urn:q", "l", "p");
        javax.xml.stream.events.Attribute attr = f.createAttribute(qn, "av");
        check(attr.getName().equals(qn) && "av".equals(attr.getValue()) && attr.isAttribute(), "createAttribute(QName,value)");
        javax.xml.stream.events.Attribute attr2 = f.createAttribute("p", "urn:q", "k", "v");
        check(attr2.getName().equals(new javax.xml.namespace.QName("urn:q", "k", "p")), "createAttribute(prefix,ns,local,value)");
        javax.xml.stream.events.Attribute attr3 = f.createAttribute("k3", "v3");
        check("k3".equals(attr3.getName().getLocalPart()) && "".equals(attr3.getName().getNamespaceURI()),
            "createAttribute(local,value): empty ns");
        check(!attr3.isSpecified(), "JDK quirk: factory-made Attribute.isSpecified() is false (parser-side attrs are true)");
        javax.xml.stream.events.StartElement se = f.createStartElement(qn,
            java.util.Collections.singletonList((javax.xml.stream.events.Attribute) attr).iterator(),
            java.util.Collections.singletonList((javax.xml.stream.events.Namespace) f.createNamespace("p", "urn:q")).iterator());
        check(se.getName().equals(qn), "createStartElement(QName, attrs, namespaces)");
        check(se.getAttributes().hasNext() && se.getNamespaces().hasNext(), "StartElement iterators carry both lists");
        check(((javax.xml.stream.events.Attribute) se.getAttributes().next()).getValue().equals("av"), "attr value via iterator");
        javax.xml.stream.events.StartElement se2 = f.createStartElement("p", "urn:q", "l",
            java.util.Collections.<javax.xml.stream.events.Attribute>emptyList().iterator(),
            java.util.Collections.<javax.xml.stream.events.Namespace>emptyList().iterator());
        check(se2.getName().equals(qn), "createStartElement(prefix,ns,local,Iterators)");
        javax.xml.stream.events.EndElement ee = f.createEndElement(qn,
            java.util.Collections.singletonList((javax.xml.stream.events.Namespace) f.createNamespace("p", "urn:q")).iterator());
        check(ee.getName().equals(qn), "createEndElement(QName, namespaces)");
        check(!ee.getNamespaces().hasNext(), "EndElement does not echo namespaces on JDK impl");
        javax.xml.stream.events.EndElement ee2 = f.createEndElement("p", "urn:q", "l");
        check(ee2.getName().equals(qn), "createEndElement(prefix,ns,local)");
        javax.xml.stream.events.Namespace ns = f.createNamespace("p", "urn:q");
        check("p".equals(ns.getPrefix()) && "urn:q".equals(ns.getNamespaceURI()), "createNamespace(prefix,uri)");
        javax.xml.stream.events.Namespace dns = f.createNamespace("urn:dflt");
        check("".equals(dns.getPrefix()) && "urn:dflt".equals(dns.getNamespaceURI()), "createNamespace(uri): default ns");
        javax.xml.stream.events.Comment cm = f.createComment("note");
        check("note".equals(cm.getText()), "createComment");
        javax.xml.stream.events.ProcessingInstruction pi = f.createProcessingInstruction("t", "d");
        check("t".equals(pi.getTarget()) && "d".equals(pi.getData()), "createProcessingInstruction");
        javax.xml.stream.events.DTD dtd = f.createDTD("<!ELEMENT a ANY>");
        check("<!ELEMENT a ANY>".equals(dtd.getDocumentTypeDeclaration()), "createDTD text");
        check(dtd.getEntities() == null && dtd.getNotations() == null, "factory-made DTD returns null entities/notations lists");
        javax.xml.stream.events.EntityReference ent = f.createEntityReference("e", null);
        check("e".equals(ent.getName()) && ent.getDeclaration() == null, "createEntityReference(name, null decl)");
        javax.xml.stream.events.Characters sp = f.createSpace("  ");
        check(sp.isWhiteSpace() && sp.getData().equals("  "), "createSpace");
        javax.xml.stream.events.Characters ig = f.createIgnorableSpace(" ");
        check(ig.isIgnorableWhiteSpace(), "createIgnorableSpace");
        javax.xml.stream.events.Characters cd = f.createCData("cc");
        check(cd.isCData() && "cc".equals(cd.getData()), "createCData");
        javax.xml.stream.events.EndDocument ed = f.createEndDocument();
        check(ed.isEndDocument(), "createEndDocument");
        javax.xml.stream.events.StartDocument sd = f.createStartDocument("UTF-8", "1.1");
        check(sd.isStartDocument() && "1.1".equals(sd.getVersion()) && "UTF-8".equals(sd.getCharacterEncodingScheme()),
            "createStartDocument(enc,ver)");
        javax.xml.stream.events.StartDocument sd2 = f.createStartDocument();
        check("1.0".equals(sd2.getVersion()), "createStartDocument() defaults version 1.0");
        javax.xml.stream.events.StartDocument sd3 = f.createStartDocument("UTF-8");
        check("UTF-8".equals(sd3.getCharacterEncodingScheme()), "createStartDocument(enc)");
    }

    // ---- Deepened 2: event type codes and flags on factory-made events ----
    static void testEventTypesAndFlags() throws Exception {
        javax.xml.stream.XMLEventFactory f = javax.xml.stream.XMLEventFactory.newFactory();
        check(f.createCharacters("x").getEventType() == 4, "Characters type 4");
        check(f.createCData("x").getEventType() == 4, "CData carried as Characters type 4 (isCData distinguishes)");
        check(f.createComment("c").getEventType() == 5, "Comment type 5");
        check(f.createNamespace("u").getEventType() == 13, "Namespace type 13");
        check(f.createAttribute("k", "v").getEventType() == 10, "Attribute type 10");
        check(f.createDTD("d").getEventType() == 11, "DTD type 11");
        check(f.createEntityReference("e", null).getEventType() == 9, "EntityReference type 9");
        check(f.createProcessingInstruction("t", "d").getEventType() == 3, "PI type 3");
        check(f.createEndDocument().getEventType() == 8, "EndDocument type 8");
        javax.xml.stream.events.XMLEvent start = f.createStartElement("", "", "a");
        check(start.getEventType() == 1 && start.isStartElement() && !start.isEndElement()
            && !start.isCharacters() && !start.isAttribute(), "StartElement flags");
        javax.xml.stream.events.XMLEvent end = f.createEndElement("", "", "a");
        check(end.getEventType() == 2 && end.isEndElement() && !end.isStartElement(), "EndElement flags");
        javax.xml.stream.events.Characters t = f.createCharacters("txt");
        check(t.isCharacters() && !t.isWhiteSpace() && !t.isCData(), "Characters flags for plain text");
        javax.xml.stream.events.Characters w = f.createSpace(" ");
        check(w.isCharacters() && w.isWhiteSpace(), "space event flags");
        // setLocation influences subsequently created events
        javax.xml.stream.Location loc = new javax.xml.stream.Location() {
            public int getLineNumber() { return 9; }
            public int getColumnNumber() { return 8; }
            public int getCharacterOffset() { return 7; }
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "sys"; }
        };
        f.setLocation(loc);
        javax.xml.stream.events.XMLEvent located = f.createCharacters("z");
        check(located.getLocation() == loc, "setLocation applies to later created events");
        check(located.getLocation().getLineNumber() == 9 && located.getLocation().getColumnNumber() == 8,
            "location values readable");
        f.setLocation(null);
    }

    // ---- Deepened 3: create -> write out -> parse back closure ----
    static void testWriteOutClosure() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.XMLOutputFactory of = javax.xml.stream.XMLOutputFactory.newFactory();
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w = of.createXMLEventWriter(sw);
        w.add(ef.createStartDocument("UTF-8", "1.0"));
        w.add(ef.createDTD("<!DOCTYPE r [<!ENTITY e \"EE\">]>"));
        w.add(ef.createStartElement("", "", "r"));
        w.add(ef.createAttribute("k", "v"));
        w.add(ef.createCharacters("txt"));
        w.add(ef.createComment("cm"));
        w.add(ef.createCData("cd"));
        w.add(ef.createProcessingInstruction("t", "d"));
        w.add(ef.createEntityReference("e", null));
        w.add(ef.createEndElement("", "", "r"));
        w.add(ef.createEndDocument());
        w.flush();
        String out = sw.toString();
        check(out.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE r [<!ENTITY e \"EE\">]><r k=\"v\">txt"),
            "closure output head, got [" + out + "]");
        check(out.contains("<!--cm-->") && out.contains("<![CDATA[cd]]>") && out.contains("<?t d?>") && out.contains("&e;"),
            "closure output comment/CDATA/PI/entity forms");
        check(out.endsWith("</r>"), "closure output tail");
        w.close();
        // parse back and verify equality with what was created
        javax.xml.stream.XMLEventReader r = javax.xml.stream.XMLInputFactory.newFactory()
            .createXMLEventReader(new java.io.StringReader(out));
        r.nextEvent(); // SD
        javax.xml.stream.events.XMLEvent dtdEv = r.nextEvent();
        check(dtdEv.getEventType() == 11
            && ((javax.xml.stream.events.DTD) dtdEv).getDocumentTypeDeclaration().contains("<!ENTITY e"),
            "round-trip DTD declares the entity");
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        check("r".equals(se.getName().getLocalPart()) && "v".equals(
            ((javax.xml.stream.events.Attribute) se.getAttributes().next()).getValue()), "round-trip attr");
        StringBuilder text = new StringBuilder();
        while (true) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e.isEndElement()) break;
            if (e.isCharacters()) text.append(e.asCharacters().getData());
        }
        check("txtcdEE".equals(text.toString()), "round-trip text incl CDATA and declared entity, got [" + text + "]");
        r.close();
    }

    // ---- Deepened 4: factory discovery ----
    static void testFactoryIdentity() throws Exception {
        javax.xml.stream.XMLEventFactory a = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.XMLEventFactory b = javax.xml.stream.XMLEventFactory.newInstance();
        check(a != null && b != null && a != b, "newFactory/newInstance fresh instances");
        check(a.getClass() == b.getClass(), "same implementation class");
        check(javax.xml.stream.XMLEventFactory.newDefaultFactory() != null,
            "JDK-27: newDefaultFactory() returns a working instance (absent in JDK-8)");
        String key = "javax.xml.stream.XMLEventFactory";
        String saved = System.getProperty(key);
        try {
            System.setProperty(key, "com.sun.xml.internal.stream.events.XMLEventFactoryImpl");
            check(javax.xml.stream.XMLEventFactory.newFactory() != null, "system property override resolves");
        } finally {
            if (saved == null) System.clearProperty(key); else System.setProperty(key, saved);
        }
    }
}
