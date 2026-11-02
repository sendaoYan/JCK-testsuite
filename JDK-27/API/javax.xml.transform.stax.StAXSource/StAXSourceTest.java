/*
 * @test
 * @summary javax.xml.transform.stax.StAXSource conformance: API type verification.
 * @run main StAXSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StAXSourceTest {
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
        Class<?> c = Class.forName("javax.xml.transform.stax.StAXSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLEventReader"), "method getXMLEventReader");
        check(hasMethod(c, "getXMLStreamReader"), "method getXMLStreamReader");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.stream.XMLStreamReader reader = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(reader);
        check(s.getXMLStreamReader() == reader, "getXMLStreamReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
