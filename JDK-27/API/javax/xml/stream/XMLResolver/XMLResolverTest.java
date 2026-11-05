/*
 * @test
 * @summary javax.xml.stream.XMLResolver conformance: white-box structure + black-box behavior.
 * @run main XMLResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLResolverTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.XMLResolver a = new javax.xml.stream.XMLResolver(){
            public java.lang.Object resolveEntity(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { return null; }
        };
        check(a != null, "anon impl of XMLResolver");
        check(a.resolveEntity("s", "s", "s", "s") == null, "resolveEntity() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testResolverInterception();
        testResolverEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: external entity intercepted and replaced (closed loop, offline) ----
    static void testResolverInterception() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        f.setProperty(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
        final boolean[] called = {false};
        final StringBuilder argsSeen = new StringBuilder();
        f.setXMLResolver(new javax.xml.stream.XMLResolver() {
            public Object resolveEntity(String publicId, String systemId, String baseURI, String namespace) {
                called[0] = true;
                argsSeen.append(publicId).append('|').append(systemId).append('|').append(baseURI).append('|').append(namespace);
                // never touch the network: hand back an inline replacement stream
                try { return new java.io.ByteArrayInputStream("<sub>RESOLVED</sub>".getBytes("UTF-8")); }
                catch (java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
            }
        });
        check(f.getXMLResolver() != null, "getXMLResolver returns installed resolver");
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE doc [<!ENTITY ext SYSTEM \"entity.xml\">]><doc>&ext;</doc>"));
        java.util.List<Integer> codes = new java.util.ArrayList<Integer>();
        StringBuilder text = new StringBuilder();
        while (r.hasNext()) {
            int ev = r.next();
            codes.add(ev);
            if (ev != 11 && r.hasText()) text.append(r.getText());
        }
        r.close();
        check(called[0], "resolver was invoked for the external entity");
        check(argsSeen.toString().equals("null|entity.xml|null|null"),
            "resolver receives (null publicId, systemId, null baseURI, null ns), got [" + argsSeen + "]");
        check(codes.equals(java.util.Arrays.asList(11, 1, 1, 4, 2, 2, 8)),
            "replacement content expands into the tree: DTD,doc,sub,text,ends, got " + codes);
        check("RESOLVED".equals(text.toString()), "replacement text visible, got [" + text + "]");
    }

    // ---- Deepened 2: resolver edge cases ----
    static void testResolverEdgeCases() throws Exception {
        // internal (inline DTD) entities are NOT routed through the resolver
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        final int[] calls = {0};
        f.setXMLResolver(new javax.xml.stream.XMLResolver() {
            public Object resolveEntity(String p, String s, String b, String n) { calls[0]++; return null; }
        });
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE d [<!ENTITY i \"INLINE\">]><d>&i;</d>"));
        while (r.hasNext()) r.next();
        r.close();
        check(calls[0] == 0, "internal entity resolved without invoking resolver");
        // resolver returning null: parser falls back to default resolution of the
        // (deliberately nonexistent) system id -> deterministic XMLStreamException, no network
        javax.xml.stream.XMLInputFactory f2 = javax.xml.stream.XMLInputFactory.newFactory();
        f2.setProperty(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
        f2.setXMLResolver(new javax.xml.stream.XMLResolver() {
            public Object resolveEntity(String p, String s, String b, String n) { return null; }
        });
        boolean thrown = false;
        javax.xml.stream.XMLStreamReader r2 = f2.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE d [<!ENTITY x SYSTEM \"no-such-file-xyz.xml\">]><d>&x;</d>"));
        try { while (r2.hasNext()) r2.next(); }
        catch (javax.xml.stream.XMLStreamException e) { thrown = true; }
        check(thrown, "null resolution falls back to system id load and fails as XMLStreamException");
        if (r2 != null) { try { r2.close(); } catch (Throwable ignore) {} }
        // no resolver installed + external entities disabled (default): doc parses, entity not expanded
        javax.xml.stream.XMLInputFactory f3 = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r3 = f3.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE d [<!ENTITY x SYSTEM \"no-such-file-xyz.xml\">]><d>safe</d>"));
        StringBuilder t3 = new StringBuilder();
        while (r3.hasNext()) { int ev = r3.next(); if (ev != 11 && r3.hasText()) t3.append(r3.getText()); }
        r3.close();
        check("safe".equals(t3.toString()), "unused external entity with default settings parses offline");
    }
}
