/*
 * @test
 * @summary javax.xml.stream.XMLEventWriter conformance: white-box structure + black-box behavior.
 * @run main XMLEventWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventWriterTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLEventWriter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.util.XMLEventConsumer"), "interface javax.xml.stream.util.XMLEventConsumer");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "setDefaultNamespace"), "method setDefaultNamespace");
        check(hasMethod(c, "setNamespaceContext"), "method setNamespaceContext");
        check(hasMethod(c, "setPrefix"), "method setPrefix");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter w = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(sw);
        w.add(ef.createStartDocument());
        w.add(ef.createStartElement("", "", "e"));
        w.add(ef.createAttribute("k", "v"));
        w.add(ef.createCharacters("hi"));
        w.add(ef.createEndElement("", "", "e"));
        w.add(ef.createEndDocument());
        w.flush();
        w.close();
        check(sw.toString().contains("<e") && sw.toString().contains("k=\"v\"") && sw.toString().contains("hi"), "event writer output, got " + sw);
        // namespace context round-trip
        javax.xml.stream.XMLEventWriter w2 = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(new java.io.StringWriter());
        w2.setDefaultNamespace("urn:d"); check("urn:d".equals(w2.getNamespaceContext().getNamespaceURI("")), "setDefaultNamespace");
        w2.setPrefix("p", "urn:p"); check("p".equals(w2.getPrefix("urn:p")), "set/getPrefix");
        w2.close();

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
