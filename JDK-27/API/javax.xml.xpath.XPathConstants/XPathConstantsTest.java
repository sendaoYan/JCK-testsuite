/*
 * @test
 * @summary javax.xml.xpath.XPathConstants conformance: white-box structure + black-box behavior.
 * @run main XPathConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathConstantsTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathConstants");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasField(c, "BOOLEAN"), "field BOOLEAN");
        check(hasField(c, "DOM_OBJECT_MODEL"), "field DOM_OBJECT_MODEL");
        check(hasField(c, "NODE"), "field NODE");
        check(hasField(c, "NODESET"), "field NODESET");
        check(hasField(c, "NUMBER"), "field NUMBER");
        check(hasField(c, "STRING"), "field STRING");

        check(javax.xml.xpath.XPathConstants.NUMBER != null, "NUMBER");
        check(javax.xml.xpath.XPathConstants.STRING != null, "STRING");
        check(javax.xml.xpath.XPathConstants.BOOLEAN != null, "BOOLEAN");
        check(javax.xml.xpath.XPathConstants.NODESET != null, "NODESET");
        check(javax.xml.xpath.XPathConstants.NODE != null, "NODE");
        check("http://java.sun.com/jaxp/xpath/dom".equals(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "DOM_OBJECT_MODEL");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("http://www.w3.org/1999/XSL/Transform".equals(javax.xml.xpath.XPathConstants.NUMBER.getNamespaceURI()), "NUMBER ns");
        check("NUMBER".equals(javax.xml.xpath.XPathConstants.NUMBER.getLocalPart()), "NUMBER local");
        check("STRING".equals(javax.xml.xpath.XPathConstants.STRING.getLocalPart()), "STRING local");
        check("BOOLEAN".equals(javax.xml.xpath.XPathConstants.BOOLEAN.getLocalPart()), "BOOLEAN local");
        check("NODESET".equals(javax.xml.xpath.XPathConstants.NODESET.getLocalPart()), "NODESET local");
        check("NODE".equals(javax.xml.xpath.XPathConstants.NODE.getLocalPart()), "NODE local");
        check("http://java.sun.com/jaxp/xpath/dom".equals(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "DOM_OBJECT_MODEL");
        check(javax.xml.xpath.XPathConstants.NUMBER != javax.xml.xpath.XPathConstants.STRING
              && javax.xml.xpath.XPathConstants.NODESET != javax.xml.xpath.XPathConstants.NODE,
              "distinct constant QNames");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
