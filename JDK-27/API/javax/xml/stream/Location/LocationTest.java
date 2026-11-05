/*
 * @test
 * @summary javax.xml.stream.Location conformance: white-box structure + black-box behavior.
 * @run main LocationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class LocationTest {
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

        Class<?> c = Class.forName("javax.xml.stream.Location");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getCharacterOffset"), "method getCharacterOffset");
        check(hasMethod(c, "getColumnNumber"), "method getColumnNumber");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.Location a = new javax.xml.stream.Location(){
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
            public int getCharacterOffset() { return 3; }
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
        };
        check(a != null, "anon impl of Location");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
        check(a.getCharacterOffset() == 3, "getCharacterOffset() returns fixed default");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCustomImplContract();
        testRealParserLocations();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: implementer contract through a fixed implementation ----
    static void testCustomImplContract() {
        javax.xml.stream.Location l = new javax.xml.stream.Location() {
            public int getLineNumber() { return 12; }
            public int getColumnNumber() { return 34; }
            public int getCharacterOffset() { return 56; }
            public String getPublicId() { return "pubId"; }
            public String getSystemId() { return "sysId"; }
            public String toString() { return "L(12,34)"; }
        };
        check(l.getLineNumber() == 12, "getLineNumber");
        check(l.getColumnNumber() == 34, "getColumnNumber");
        check(l.getCharacterOffset() == 56, "getCharacterOffset");
        check("pubId".equals(l.getPublicId()), "getPublicId");
        check("sysId".equals(l.getSystemId()), "getSystemId");
        check("L(12,34)".equals(String.valueOf(l)), "toString override honored");
        // all-negative/-1 sentinel values are legal (used for unknown fields)
        javax.xml.stream.Location u = new javax.xml.stream.Location() {
            public int getLineNumber() { return -1; }
            public int getColumnNumber() { return -1; }
            public int getCharacterOffset() { return -1; }
            public String getPublicId() { return null; }
            public String getSystemId() { return null; }
        };
        check(u.getLineNumber() == -1 && u.getColumnNumber() == -1 && u.getCharacterOffset() == -1
            && u.getPublicId() == null && u.getSystemId() == null, "-1/null sentinels pass through");
    }

    // ---- Deepened 2: real parser Location values over a fixed multi-line document ----
    static void testRealParserLocations() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<r>\n  <a/>\n</r>"));
        r.next(); // START r
        javax.xml.stream.Location l0 = r.getLocation();
        check(l0.getLineNumber() == 1 && l0.getColumnNumber() == 4,
            "START r at line 1 col 4, got " + l0.getLineNumber() + "," + l0.getColumnNumber());
        r.next(); r.next(); // ws, START a
        javax.xml.stream.Location l1 = r.getLocation();
        check(l1.getLineNumber() == 2, "element a reported on line 2, got " + l1.getLineNumber());
        check(l1.getColumnNumber() >= 1, "column >= 1");
        check(l1.getCharacterOffset() == 10, "character offset 10, got " + l1.getCharacterOffset());
        check(l0.getPublicId() == null && l0.getSystemId() == null, "Reader input has no ids");
        String s = String.valueOf(l1);
        check(s.contains("Line number = 2") && s.contains("Column number = ")
            && s.contains("CharacterOffset"), "Location.toString exposes all fields");
        r.close();
        // parse error location precision
        try {
            javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader(
                "<r>\n  <a>\n</r>"));
            while (r2.hasNext()) r2.next();
            check(false, "unclosed element must throw");
        } catch (javax.xml.stream.XMLStreamException e) {
            check(e.getLocation().getLineNumber() == 3, "error location points at line 3, got "
                + e.getLocation().getLineNumber());
        }
    }
}
