/*
 * @test
 * @summary javax.xml.stream.events.Comment conformance: white-box structure + black-box behavior.
 * @run main CommentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CommentTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.Comment");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getText"), "method getText");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.Comment a = new javax.xml.stream.events.Comment(){
            public java.lang.String getText() { return "s"; }
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
        check(a != null, "anon impl of Comment");
        check("s".equals(a.getText()), "getText() returns fixed default");
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

    // ---- Part 2: contract deepening (factory + parsed + writer round-trip) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createComment: text equality, code, family, encoded form
        javax.xml.stream.events.Comment cm = ef.createComment(" note ");
        check(" note ".equals(cm.getText()), "factory comment text equality");
        check(cm.getEventType() == 5, "comment code 5");
        check(!cm.isCharacters() && !cm.isStartElement() && !cm.isEndElement() && !cm.isStartDocument()
                && !cm.isEndDocument() && !cm.isProcessingInstruction(), "comment isXxx family all false");
        java.io.StringWriter w = new java.io.StringWriter();
        cm.writeAsEncodedUnicode(w);
        check("<!-- note -->".equals(w.toString()), "encoded <!-- note -->");
        // special XML chars kept verbatim inside comment
        javax.xml.stream.events.Comment cm2 = ef.createComment("a<b>&\"'");
        java.io.StringWriter w2 = new java.io.StringWriter();
        cm2.writeAsEncodedUnicode(w2);
        check("<!--a<b>&\"'-->".equals(w2.toString()), "comment content not escaped");
        // createComment(null) tolerated
        javax.xml.stream.events.Comment cmn = ef.createComment(null);
        check(cmn != null && cmn.getText() == null, "createComment(null) getText null");
        try { cm.asCharacters(); check(false, "Comment.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed comments: before-root and after-root, text exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<!--c1--><r>x</r><!--tail-->"));
        r.nextEvent(); // START_DOCUMENT always first
        javax.xml.stream.events.XMLEvent e = r.nextEvent();
        check(e.getEventType() == 5 && "c1".equals(((javax.xml.stream.events.Comment) e).getText()),
                "prolog comment c1");
        javax.xml.stream.events.Comment tail = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent x = r.nextEvent();
            if (x.getEventType() == 5) tail = (javax.xml.stream.events.Comment) x;
        }
        check(tail != null && "tail".equals(tail.getText()), "epilog comment tail");
        check(tail.getLocation().getLineNumber() >= 1, "comment location line >= 1");
        r.close();

        // 3) comment inside element content preserved in order
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r>a<!--mid-->b</r>"));
        r2.nextEvent(); r2.nextEvent();
        check("a".equals(r2.nextEvent().asCharacters().getData()), "text a");
        javax.xml.stream.events.XMLEvent mid = r2.nextEvent();
        check(mid.getEventType() == 5 && "mid".equals(((javax.xml.stream.events.Comment) mid).getText()), "mid comment");
        check("b".equals(r2.nextEvent().asCharacters().getData()), "text b");
        r2.close();

        // 4) round-trip: factory comment -> XMLEventWriter -> output contains exact form
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createComment("c1"));
        ew.add(ef.createEndDocument());
        ew.flush();
        String out = new String(bos.toByteArray(), "UTF-8");
        check(out.contains("<!--c1-->"), "writer round-trip contains <!--c1-->");
        ew.close();
    }
}
