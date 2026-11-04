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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
