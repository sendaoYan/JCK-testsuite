/*
 * @test
 * @summary javax.xml.stream.events.XMLEvent conformance: API type verification.
 * @run main XMLEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.XMLEvent");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.XMLStreamConstants"), "interface javax.xml.stream.XMLStreamConstants");
        check(hasMethod(c, "asCharacters"), "method asCharacters");
        check(hasMethod(c, "asEndElement"), "method asEndElement");
        check(hasMethod(c, "asStartElement"), "method asStartElement");
        check(hasMethod(c, "getEventType"), "method getEventType");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getSchemaType"), "method getSchemaType");
        check(hasMethod(c, "isAttribute"), "method isAttribute");
        check(hasMethod(c, "isCharacters"), "method isCharacters");
        check(hasMethod(c, "isEndDocument"), "method isEndDocument");
        check(hasMethod(c, "isEndElement"), "method isEndElement");
        check(hasMethod(c, "isEntityReference"), "method isEntityReference");
        check(hasMethod(c, "isNamespace"), "method isNamespace");
        check(hasMethod(c, "isProcessingInstruction"), "method isProcessingInstruction");
        check(hasMethod(c, "isStartDocument"), "method isStartDocument");
        check(hasMethod(c, "isStartElement"), "method isStartElement");
        check(hasMethod(c, "writeAsEncodedUnicode"), "method writeAsEncodedUnicode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
