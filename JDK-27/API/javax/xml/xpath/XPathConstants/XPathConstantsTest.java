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
        testQNameContract();
        testUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // The five return-type QNames use the XPath 1.0 (= XSLT) namespace, NOT the
    // XPath language URI; locked actual per javadoc and runtime.
    static void testQNameContract() {
        String NS = "http://www.w3.org/1999/XSL/Transform";
        javax.xml.namespace.QName[] all = {
            javax.xml.xpath.XPathConstants.NUMBER,
            javax.xml.xpath.XPathConstants.STRING,
            javax.xml.xpath.XPathConstants.BOOLEAN,
            javax.xml.xpath.XPathConstants.NODESET,
            javax.xml.xpath.XPathConstants.NODE,
        };
        String[] locals = {"NUMBER", "STRING", "BOOLEAN", "NODESET", "NODE"};
        for (int i = 0; i < all.length; i++) {
            check(NS.equals(all[i].getNamespaceURI()), locals[i] + " namespace is XSL/XPath 1.0 ns");
            check(locals[i].equals(all[i].getLocalPart()), locals[i] + " localPart exact");
        }
        // constants are static final: identical instances on repeated reads
        check(javax.xml.xpath.XPathConstants.NUMBER == javax.xml.xpath.XPathConstants.NUMBER,
              "constants are identity-stable");
        // all five distinct from one another
        java.util.Set<javax.xml.namespace.QName> set = new java.util.HashSet<javax.xml.namespace.QName>();
        for (javax.xml.namespace.QName q : all) set.add(q);
        check(set.size() == 5, "five distinct QNames");
        // prefix not part of these constants
        check(javax.xml.xpath.XPathConstants.NUMBER.getPrefix() == null
              || javax.xml.xpath.XPathConstants.NUMBER.getPrefix().isEmpty(),
              "NUMBER prefix null/empty");
        // equality by (ns, local) per QName contract
        check(javax.xml.xpath.XPathConstants.STRING
                  .equals(new javax.xml.namespace.QName(NS, "STRING")),
              "STRING equals same (ns, local) QName built by caller");
    }

    // ---- each constant drives the matching evaluate return type (closed loop) ----
    static void testUsage() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document d = dbf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root><a>1</a><a>2</a></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        check(xp.evaluate("count(/root/a)", d, javax.xml.xpath.XPathConstants.NUMBER) instanceof Double,
              "NUMBER drives Double result");
        check(xp.evaluate("/root/a[1]", d, javax.xml.xpath.XPathConstants.STRING) instanceof String,
              "STRING drives String result");
        check(xp.evaluate("count(/root/a) = 2", d, javax.xml.xpath.XPathConstants.BOOLEAN) instanceof Boolean,
              "BOOLEAN drives Boolean result");
        check(xp.evaluate("/root/a", d, javax.xml.xpath.XPathConstants.NODESET) instanceof org.w3c.dom.NodeList,
              "NODESET drives NodeList result");
        check(xp.evaluate("/root/a[1]", d, javax.xml.xpath.XPathConstants.NODE) instanceof org.w3c.dom.Node,
              "NODE drives Node result");

        // DOM_OBJECT_MODEL string is accepted by the factory
        check(javax.xml.xpath.XPathFactory.newInstance(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL) != null,
              "DOM_OBJECT_MODEL accepted by XPathFactory.newInstance");
    }
}

