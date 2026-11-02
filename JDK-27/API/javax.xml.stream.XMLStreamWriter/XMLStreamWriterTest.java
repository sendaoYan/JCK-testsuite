/*
 * @test
 * @summary javax.xml.stream.XMLStreamWriter conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
