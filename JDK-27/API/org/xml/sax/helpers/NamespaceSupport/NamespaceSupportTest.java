/*
 * @test
 * @summary org.xml.sax.helpers.NamespaceSupport conformance: white-box structure + black-box behavior.
 * @run main NamespaceSupportTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NamespaceSupportTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.NamespaceSupport");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "declarePrefix"), "method declarePrefix");
        check(hasMethod(c, "getDeclaredPrefixes"), "method getDeclaredPrefixes");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "getPrefixes"), "method getPrefixes");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "isNamespaceDeclUris"), "method isNamespaceDeclUris");
        check(hasMethod(c, "popContext"), "method popContext");
        check(hasMethod(c, "processName"), "method processName");
        check(hasMethod(c, "pushContext"), "method pushContext");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setNamespaceDeclUris"), "method setNamespaceDeclUris");
        check(hasField(c, "NSDECL"), "field NSDECL");
        check(hasField(c, "XMLNS"), "field XMLNS");

        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        ns.pushContext();
        check(ns.declarePrefix("p", "http://ns"), "declarePrefix");
        check("http://ns".equals(ns.getURI("p")), "getURI");
        check("p".equals(ns.getPrefix("http://ns")), "getPrefix");
        ns.popContext();
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        check("http://www.w3.org/XML/1998/namespace".equals(org.xml.sax.helpers.NamespaceSupport.XMLNS), "XMLNS");
        check("http://www.w3.org/xmlns/2000/".equals(org.xml.sax.helpers.NamespaceSupport.NSDECL), "NSDECL");
        check(ns.getURI("xml").equals("http://www.w3.org/XML/1998/namespace"), "xml prefix built-in");
        ns.declarePrefix("p", "urn:p");
        check("urn:p".equals(ns.getURI("p")), "declarePrefix + getURI");
        check("p".equals(ns.getPrefix("urn:p")), "getPrefix");
        ns.pushContext();
        ns.declarePrefix("p", "urn:p2");
        check("urn:p2".equals(ns.getURI("p")), "inner context shadow");
        ns.popContext();
        check("urn:p".equals(ns.getURI("p")), "pop restores outer");
        check(!ns.isNamespaceDeclUris(), "namespaceDeclUris default false");
        ns.setNamespaceDeclUris(true); check(ns.isNamespaceDeclUris(), "setNamespaceDeclUris(true)");
        ns.reset();
        check(ns.getURI("p") == null, "reset clears prefixes");
        String[] parts = new String[3];
        ns.declarePrefix("q", "urn:q");
        ns.processName("q:local", parts, false);
        check("urn:q".equals(parts[0]) && "local".equals(parts[1]) && "q:local".equals(parts[2]), "processName splits");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral NamespaceSupport contract (context stack + processName matrix) ----
    static void deepContract() throws Exception {
        check("http://www.w3.org/XML/1998/namespace".equals(org.xml.sax.helpers.NamespaceSupport.XMLNS), "XMLNS constant is the xml-prefix namespace URI");
        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        ns.pushContext();
        check("http://www.w3.org/XML/1998/namespace".equals(ns.getURI("xml")), "xml prefix pre-bound without any declaration");
        check(!ns.declarePrefix("xml", "http://www.w3.org/XML/1998/namespace"), "declarePrefix('xml', official URI) refused");
        check(!ns.declarePrefix("xml", "urn:wrong"), "declarePrefix('xml', wrong URI) refused");
        check(ns.declarePrefix("p", "urn:p"), "declarePrefix('p') accepted");
        String[] r = ns.processName("p:x", new String[3], false);
        check("urn:p".equals(r[0]) && "x".equals(r[1]) && "p:x".equals(r[2]), "processName returns [uri, local, raw]");
        check(ns.processName("q:x", new String[3], false) == null, "undeclared prefix -> null (not an exception)");
        check(ns.processName("xmlns:e", new String[3], false) == null, "xmlns-prefixed ELEMENT name -> null");
        // default namespace: elements adopt it, attributes never do
        check(ns.declarePrefix("", "urn:d"), "declaring the empty prefix is allowed");
        check("urn:d".equals(ns.getURI("")), "getURI('') returns the default namespace");
        String[] elem = ns.processName("bare", new String[3], false);
        String[] attr = ns.processName("bare", new String[3], true);
        check("urn:d".equals(elem[0]) && "bare".equals(elem[1]), "unprefixed element name adopts the default namespace");
        check("".equals(attr[0]), "unprefixed attribute name never adopts the default namespace");
        ns.declarePrefix("", "");
        check(ns.getURI("") == null, "declarePrefix('','') undeclares the default namespace");
        check("".equals(ns.processName("b2", new String[3], false)[0]), "element back in no-namespace after undeclare");
        ns.declarePrefix("", "urn:d");
        // prefix enumerations
        java.util.List<String> all = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getPrefixes(); e.hasMoreElements(); ) all.add((String) e.nextElement());
        check(all.contains("p") && all.contains("xml"), "getPrefixes includes declared + built-in xml");
        java.util.List<String> decl = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getDeclaredPrefixes(); e.hasMoreElements(); ) decl.add((String) e.nextElement());
        check(decl.contains("p") && decl.contains("") && !decl.contains("xml"), "getDeclaredPrefixes is current-context only (no xml)");
        check("p".equals(ns.getPrefix("urn:p")), "getPrefix reverse lookup");
        // context stack
        ns.pushContext();
        check("urn:p".equals(ns.getURI("p")), "child context inherits parent declarations");
        ns.declarePrefix("p", "urn:p2");
        check("urn:p2".equals(ns.getURI("p")), "child may rebind an inherited prefix");
        ns.popContext();
        check("urn:p".equals(ns.getURI("p")), "popContext restores the outer binding");
        // reset
        ns.reset();
        check("http://www.w3.org/XML/1998/namespace".equals(ns.getURI("xml")), "reset keeps the built-in xml binding");
        java.util.List<String> afterReset = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getPrefixes(); e.hasMoreElements(); ) afterReset.add((String) e.nextElement());
        check(afterReset.contains("xml"), "reset leaves xml enumerable");
        check(ns.getURI("p") == null, "reset drops user declarations");
    }
}
