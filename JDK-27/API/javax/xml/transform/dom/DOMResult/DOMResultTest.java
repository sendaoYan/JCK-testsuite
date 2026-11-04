/*
 * @test
 * @summary javax.xml.transform.dom.DOMResult conformance: white-box structure + black-box behavior.
 * @run main DOMResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.dom.DOMResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getNextSibling"), "method getNextSibling");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "setNextSibling"), "method setNextSibling");
        check(hasMethod(c, "setNode"), "method setNode");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult();
        check(r.getNode() == null, "node null");
        r.setSystemId("sys");
        check("sys".equals(r.getSystemId()), "systemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.newDocument();
        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult(d);
        check(r.getNode() == d, "node round-trip via Document ctor");
        javax.xml.transform.dom.DOMResult r2 = new javax.xml.transform.dom.DOMResult();
        check(r2.getNode() == null && r2.getNextSibling() == null, "no-arg empty");
        org.w3c.dom.Element el = d.createElement("e");
        javax.xml.transform.dom.DOMResult r3 = new javax.xml.transform.dom.DOMResult(el, (org.w3c.dom.Node) null);
        check(r3.getNode() == el, "node round-trip via Element ctor");
        r3.setSystemId("sys"); check("sys".equals(r3.getSystemId()), "systemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
