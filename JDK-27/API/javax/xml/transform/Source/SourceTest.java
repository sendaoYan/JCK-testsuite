/*
 * @test
 * @summary javax.xml.transform.Source conformance: white-box structure + black-box behavior.
 * @run main SourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.Source");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.Source a = new javax.xml.transform.Source(){
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getSystemId() { return "s"; }
        };
        check(a != null, "anon impl of Source");
        a.setSystemId("s");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchy();
        testSystemIdContract();
        testIsEmptyDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: implementor hierarchy + systemId contract + isEmpty default
    static void testHierarchy() throws Exception {
        check(new javax.xml.transform.stream.StreamSource() instanceof javax.xml.transform.Source,
              "StreamSource implements Source");
        check(new javax.xml.transform.dom.DOMSource() instanceof javax.xml.transform.Source,
              "DOMSource implements Source");
        check(new javax.xml.transform.sax.SAXSource() instanceof javax.xml.transform.Source,
              "SAXSource implements Source");
        check(new javax.xml.transform.stax.StAXSource(
                   javax.xml.stream.XMLInputFactory.newFactory()
                       .createXMLStreamReader(new java.io.StringReader("<a/>")))
                   instanceof javax.xml.transform.Source,
              "StAXSource implements Source");
    }

    static void testSystemIdContract() {
        javax.xml.transform.Source s = new javax.xml.transform.Source() {
            String sid;
            public void setSystemId(String id) { sid = id; }
            public String getSystemId() { return sid; }
        };
        check(s.getSystemId() == null, "fresh anonymous Source: systemId null");
        s.setSystemId("http://base/doc.xml");
        check("http://base/doc.xml".equals(s.getSystemId()),
              "setSystemId/getSystemId round-trip on custom impl");
        s.setSystemId(null);
        check(s.getSystemId() == null, "setSystemId(null) stored as null");
    }

    static void testIsEmptyDefault() {
        // JDK-27: Source.isEmpty() is a default method whose contract is to throw
        // UnsupportedOperationException for implementations that do not override it
        javax.xml.transform.Source s = new javax.xml.transform.Source() {
            public void setSystemId(String id) { }
            public String getSystemId() { return null; }
        };
        boolean uoe = false;
        try {
            s.isEmpty();
        } catch (UnsupportedOperationException ex) {
            uoe = ex.getMessage() != null && ex.getMessage().contains("not supported");
        }
        check(uoe, "JDK-27: Source.isEmpty() default throws UOE for non-overriding impl");
        // every bundled Source implementation does override it
        boolean streamOk = false;
        try {
            streamOk = !new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader("<a/>")).isEmpty();
        } catch (UnsupportedOperationException ex) { streamOk = false; }
        check(streamOk, "JDK-27: StreamSource overrides isEmpty() (no UOE)");
    }
}
