/*
 * @test
 * @summary javax.xml.crypto.dom.DOMStructure conformance: white-box structure + black-box behavior.
 * @run main DOMStructureTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMStructureTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMStructure");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "isFeatureSupported"), "method isFeatureSupported");

        org.w3c.dom.Node n = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("a");
        javax.xml.crypto.dom.DOMStructure s = new javax.xml.crypto.dom.DOMStructure(n);
        check(s.getNode() == n, "getNode");
        check(!s.isFeatureSupported("nonexistent"), "isFeatureSupported");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        javax.xml.crypto.dom.DOMStructure ds = new javax.xml.crypto.dom.DOMStructure(el);
        check(ds.getNode() == el, "getNode returns ctor node");
        check(!ds.isFeatureSupported("x"), "isFeatureSupported unknown -> false");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
