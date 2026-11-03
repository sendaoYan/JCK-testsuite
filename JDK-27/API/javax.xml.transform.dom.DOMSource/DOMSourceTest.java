/*
 * @test
 * @summary javax.xml.transform.dom.DOMSource conformance: white-box structure + black-box behavior.
 * @run main DOMSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMSourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.dom.DOMSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setNode"), "method setNode");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource();
        check(s.getNode() == null, "node null");
        check(s.isEmpty(), "isEmpty");
        s.setSystemId("sys");
        check("sys".equals(s.getSystemId()), "systemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource(d);
        check(s.getNode() == d, "node round-trip");
        javax.xml.transform.dom.DOMSource s2 = new javax.xml.transform.dom.DOMSource();
        check(s2.getNode() == null && s2.getSystemId() == null, "no-arg empty");
        org.w3c.dom.Node n = d.getDocumentElement();
        javax.xml.transform.dom.DOMSource s3 = new javax.xml.transform.dom.DOMSource(n, "sys");
        check(s3.getNode() == n && "sys".equals(s3.getSystemId()), "node + systemId ctor");
        s3.setSystemId("s2"); check("s2".equals(s3.getSystemId()), "setSystemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
