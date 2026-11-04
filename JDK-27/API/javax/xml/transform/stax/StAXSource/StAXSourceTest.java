/*
 * @test
 * @summary javax.xml.transform.stax.StAXSource conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventReader er = javax.xml.stream.XMLInputFactory.newFactory().createXMLEventReader(new java.io.StringReader("<r/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(er);
        check(s.getXMLEventReader() == er, "event reader round-trip");
        boolean sok = false;
        try { s.setSystemId("sys"); }
        catch (UnsupportedOperationException e) { sok = true; }
        check(sok, "setSystemId -> UnsupportedOperationException");
        javax.xml.stream.XMLStreamReader sr = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(new java.io.StringReader("<r/>"));
        javax.xml.transform.stax.StAXSource s2 = new javax.xml.transform.stax.StAXSource(sr);
        check(s2.getXMLStreamReader() == sr, "stream reader round-trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
