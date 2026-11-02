/*
 * @test
 * @summary javax.xml.stream.XMLOutputFactory conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
