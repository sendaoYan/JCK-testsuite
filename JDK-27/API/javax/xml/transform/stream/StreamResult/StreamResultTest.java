/*
 * @test
 * @summary javax.xml.transform.stream.StreamResult conformance: white-box structure + black-box behavior.
 * @run main StreamResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StreamResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.stream.StreamResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getWriter"), "method getWriter");
        check(hasMethod(c, "setOutputStream"), "method setOutputStream");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasMethod(c, "setWriter"), "method setWriter");
        check(hasField(c, "FEATURE"), "field FEATURE");

        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult(sw);
        check(r.getWriter() == sw, "getWriter");
        check(r.getSystemId() == null, "systemId null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult(bos);
        check(r.getOutputStream() == bos, "output stream round-trip");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r2 = new javax.xml.transform.stream.StreamResult(sw);
        check(r2.getWriter() == sw, "writer round-trip");
        javax.xml.transform.stream.StreamResult r3 = new javax.xml.transform.stream.StreamResult("file.xml");
        check("file.xml".equals(r3.getSystemId()), "systemId ctor");
        r3.setSystemId("s"); check("s".equals(r3.getSystemId()), "setSystemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
