/*
 * @test
 * @summary org.xml.sax.ext.EntityResolver2 conformance: white-box structure + black-box behavior.
 * @run main EntityResolver2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityResolver2Test {
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

        Class<?> c = Class.forName("org.xml.sax.ext.EntityResolver2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasMethod(c, "getExternalSubset"), "method getExternalSubset");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.EntityResolver2 a = new org.xml.sax.ext.EntityResolver2(){
            public org.xml.sax.InputSource getExternalSubset(java.lang.String p0, java.lang.String p1) { return null; }
            public org.xml.sax.InputSource resolveEntity(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { return null; }
            public org.xml.sax.InputSource resolveEntity(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of EntityResolver2");
        check(a.getExternalSubset("s", "s") == null, "getExternalSubset() returns fixed default");
        check(a.resolveEntity("s", "s", "s", "s") == null, "resolveEntity() returns fixed default");
        check(a.resolveEntity("s", "s") == null, "resolveEntity() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EntityResolver2 contract (4-arg resolution channel) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.EntityResolver.class.isAssignableFrom(org.xml.sax.ext.EntityResolver2.class), "EntityResolver2 extends EntityResolver");
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        org.xml.sax.ext.EntityResolver2 er2 = new org.xml.sax.ext.EntityResolver2() {
            public org.xml.sax.InputSource getExternalSubset(String name, String baseURI) { calls.add("subset(" + name + ")"); return null; }
            public org.xml.sax.InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) { calls.add("resolve4(" + name + "," + systemId + ")"); return null; }
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) { calls.add("resolve2(" + systemId + ")"); return null; }
        };
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setEntityResolver(er2);
        boolean fnf = false;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(calls.contains("resolve4(null,missing.dtd)"), "external DTD resolution goes through the 4-arg resolveEntity");
        check(fnf, "returning null defers to default resolution (missing file -> FileNotFoundException)");
        // 2-arg channel used when resolver is only an EntityResolver (JDK-8 legacy path)
        final java.util.List<String> calls2 = new java.util.ArrayList<String>();
        org.xml.sax.EntityResolver er1 = new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) { calls2.add("resolve2(" + systemId + ")"); return null; }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setEntityResolver(er1);
        check(xr2.getEntityResolver() == er1, "setEntityResolver/getEntityResolver round-trip");
        boolean fnf2 = false;
        try { xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf2 = true; }
        check(fnf2, "plain EntityResolver null return also defers to default resolution");
    }
}
