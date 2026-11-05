/*
 * @test
 * @summary org.xml.sax.EntityResolver conformance: white-box structure + black-box behavior.
 * @run main EntityResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityResolverTest {
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

        Class<?> c = Class.forName("org.xml.sax.EntityResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.EntityResolver a = new org.xml.sax.EntityResolver(){
            public org.xml.sax.InputSource resolveEntity(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of EntityResolver");
        check(a.resolveEntity("s", "s") == null, "resolveEntity() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EntityResolver contract (substitution + default) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.InputSource[] substitution = new org.xml.sax.InputSource[1];
        final java.util.List<String> asked = new java.util.ArrayList<String>();
        xr.setEntityResolver(new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                asked.add(systemId);
                return substitution[0];
            }
        });
        final StringBuilder body = new StringBuilder();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void characters(char[] c, int s, int n) { body.append(c, s, n); }
        });
        // returning a replacement InputSource: its content is used
        substitution[0] = new org.xml.sax.InputSource(new java.io.StringReader("[resolved]"));
        String xml = "<!DOCTYPE r [<!ENTITY e SYSTEM 'ent.xml'>]><r>&e;</r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(body.toString().contains("[resolved]"), "resolver-provided InputSource content flows into characters");
        check(!asked.isEmpty(), "resolveEntity consulted with the (absolutized) systemId");
        // returning null -> default resolution (missing file -> FileNotFoundException)
        substitution[0] = null;
        boolean fnf = false;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(fnf, "resolver returning null falls back to default resolution (missing file -> FileNotFoundException)");
        check(xr.getEntityResolver() != null, "getEntityResolver round-trip");
    }
}
