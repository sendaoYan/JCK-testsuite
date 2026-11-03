/*
 * @test
 * @summary javax.xml.stream.XMLOutputFactory conformance: white-box structure + black-box behavior.
 * @run main XMLOutputFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLOutputFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLOutputFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "createXMLEventWriter"), "method createXMLEventWriter");
        check(hasMethod(c, "createXMLStreamWriter"), "method createXMLStreamWriter");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "isPropertySupported"), "method isPropertySupported");
        check(hasMethod(c, "newDefaultFactory"), "method newDefaultFactory");
        check(hasMethod(c, "newFactory"), "method newFactory");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasField(c, "IS_REPAIRING_NAMESPACES"), "field IS_REPAIRING_NAMESPACES");

        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(sw);
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        check(sw.toString().contains("root"), "output contains root");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        check(f != null, "newFactory");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(bos, "UTF-8");
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("x");
        w.writeAttribute("k", "v");
        w.writeCharacters("hi");
        w.writeEndElement();
        w.writeEndDocument();
        w.close();
        check("<?xml version=\"1.0\" encoding=\"UTF-8\"?><x k=\"v\">hi</x>".equals(bos.toString("UTF-8")), "stream writer exact output, got " + bos.toString("UTF-8"));
        // XMLEventWriter
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter ew = f.createXMLEventWriter(sw);
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        ew.add(ef.createStartDocument());
        ew.add(ef.createStartElement("", "", "e"));
        ew.add(ef.createEndElement("", "", "e"));
        ew.add(ef.createEndDocument());
        ew.close();
        check(sw.toString().contains("<e") && sw.toString().contains("</e>"), "event writer output contains element");
        check(!f.isPropertySupported("http://example.invalid/nope"), "unknown property unsupported");
        boolean ok = false;
        try { f.getProperty("http://example.invalid/nope"); }
        catch (IllegalArgumentException e) { ok = true; }
        check(ok, "getProperty(unknown) -> IllegalArgumentException");
        check(javax.xml.stream.XMLOutputFactory.newInstance() != null, "newInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
