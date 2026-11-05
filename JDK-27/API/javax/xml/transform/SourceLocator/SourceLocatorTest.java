/*
 * @test
 * @summary javax.xml.transform.SourceLocator conformance: white-box structure + black-box behavior.
 * @run main SourceLocatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SourceLocatorTest {
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

        Class<?> c = Class.forName("javax.xml.transform.SourceLocator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getColumnNumber"), "method getColumnNumber");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.SourceLocator a = new javax.xml.transform.SourceLocator(){
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
        };
        check(a != null, "anon impl of SourceLocator");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceSurface();
        testLocatorCarryingException();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: all-abstract surface + locator carried by TransformerException
    static void testInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.SourceLocator");
        java.lang.reflect.Method[] ms = c.getMethods();
        check(ms.length == 4, "exactly 4 methods (publicId/systemId/line/column)");
        boolean allAbstract = true;
        for (java.lang.reflect.Method m : ms) {
            if (!java.lang.reflect.Modifier.isAbstract(m.getModifiers())) allAbstract = false;
        }
        check(allAbstract, "all SourceLocator methods abstract (no defaults)");
    }

    static void testLocatorCarryingException() {
        javax.xml.transform.SourceLocator loc = new javax.xml.transform.SourceLocator() {
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 7; }
            public int getColumnNumber() { return 11; }
        };
        check("pub".equals(loc.getPublicId()) && "http://base/x.xml".equals(loc.getSystemId()),
              "locator getters");
        check(loc.getLineNumber() == 7 && loc.getColumnNumber() == 11, "line/column getters");
        javax.xml.transform.TransformerException e =
            new javax.xml.transform.TransformerException("m", loc);
        check(e.getLocator() instanceof javax.xml.transform.SourceLocator,
              "TransformerException carries its SourceLocator");
        String las = e.getLocationAsString();
        check(las != null && las.contains("7") && las.contains("11"),
              "getLocationAsString renders line/column");
        String mal = e.getMessageAndLocation();
        check(mal != null && mal.startsWith("m"), "getMessageAndLocation starts with message");
    }
}
