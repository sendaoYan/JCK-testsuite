/*
 * @test
 * @summary javax.xml.stream.XMLStreamWriter conformance: white-box structure + black-box behavior.
 * @run main XMLStreamWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLStreamWriterTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLStreamWriter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "setDefaultNamespace"), "method setDefaultNamespace");
        check(hasMethod(c, "setNamespaceContext"), "method setNamespaceContext");
        check(hasMethod(c, "setPrefix"), "method setPrefix");
        check(hasMethod(c, "writeAttribute"), "method writeAttribute");
        check(hasMethod(c, "writeCData"), "method writeCData");
        check(hasMethod(c, "writeCharacters"), "method writeCharacters");
        check(hasMethod(c, "writeComment"), "method writeComment");
        check(hasMethod(c, "writeDTD"), "method writeDTD");
        check(hasMethod(c, "writeDefaultNamespace"), "method writeDefaultNamespace");
        check(hasMethod(c, "writeEmptyElement"), "method writeEmptyElement");
        check(hasMethod(c, "writeEndDocument"), "method writeEndDocument");
        check(hasMethod(c, "writeEndElement"), "method writeEndElement");
        check(hasMethod(c, "writeEntityRef"), "method writeEntityRef");
        check(hasMethod(c, "writeNamespace"), "method writeNamespace");
        check(hasMethod(c, "writeProcessingInstruction"), "method writeProcessingInstruction");
        check(hasMethod(c, "writeStartDocument"), "method writeStartDocument");
        check(hasMethod(c, "writeStartElement"), "method writeStartElement");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.XMLStreamWriter a = new javax.xml.stream.XMLStreamWriter(){
            public void writeStartElement(java.lang.String p0) { }
            public void writeStartElement(java.lang.String p0, java.lang.String p1) { }
            public void writeStartElement(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void writeEmptyElement(java.lang.String p0, java.lang.String p1) { }
            public void writeEmptyElement(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void writeEmptyElement(java.lang.String p0) { }
            public void writeEndElement() { }
            public void writeEndDocument() { }
            public void close() { }
            public void flush() { }
            public void writeAttribute(java.lang.String p0, java.lang.String p1) { }
            public void writeAttribute(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { }
            public void writeAttribute(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void writeNamespace(java.lang.String p0, java.lang.String p1) { }
            public void writeDefaultNamespace(java.lang.String p0) { }
            public void writeComment(java.lang.String p0) { }
            public void writeProcessingInstruction(java.lang.String p0) { }
            public void writeProcessingInstruction(java.lang.String p0, java.lang.String p1) { }
            public void writeCData(java.lang.String p0) { }
            public void writeDTD(java.lang.String p0) { }
            public void writeEntityRef(java.lang.String p0) { }
            public void writeStartDocument() { }
            public void writeStartDocument(java.lang.String p0) { }
            public void writeStartDocument(java.lang.String p0, java.lang.String p1) { }
            public void writeCharacters(java.lang.String p0) { }
            public void writeCharacters(char[] p0, int p1, int p2) { }
            public java.lang.String getPrefix(java.lang.String p0) { return "s"; }
            public void setPrefix(java.lang.String p0, java.lang.String p1) { }
            public void setDefaultNamespace(java.lang.String p0) { }
            public void setNamespaceContext(javax.xml.namespace.NamespaceContext p0) { }
            public javax.xml.namespace.NamespaceContext getNamespaceContext() { return null; }
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of XMLStreamWriter");
        a.writeStartElement("s");
        a.writeStartElement("s", "s");
        a.writeStartElement("s", "s", "s");
        a.writeEmptyElement("s", "s");
        a.writeEmptyElement("s", "s", "s");
        a.writeEmptyElement("s");
        a.writeEndElement();
        a.writeEndDocument();
        a.close();
        a.flush();
        a.writeAttribute("s", "s");
        a.writeAttribute("s", "s", "s", "s");
        a.writeAttribute("s", "s", "s");
        a.writeNamespace("s", "s");
        a.writeDefaultNamespace("s");
        a.writeComment("s");
        a.writeProcessingInstruction("s");
        a.writeProcessingInstruction("s", "s");
        a.writeCData("s");
        a.writeDTD("s");
        a.writeEntityRef("s");
        a.writeStartDocument();
        a.writeStartDocument("s");
        a.writeStartDocument("s", "s");
        a.writeCharacters("s");
        a.writeCharacters((char[]) null, 3, 3);
        check("s".equals(a.getPrefix("s")), "getPrefix() returns fixed default");
        a.setPrefix("s", "s");
        a.setDefaultNamespace("s");
        a.setNamespaceContext((javax.xml.namespace.NamespaceContext) null);
        check(a.getNamespaceContext() == null, "getNamespaceContext() returns fixed default");
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
