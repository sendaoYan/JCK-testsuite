/*
 * @test
 * @summary org.xml.sax.InputSource conformance: white-box structure + black-box behavior.
 * @run main InputSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class InputSourceTest {
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

        Class<?> c = Class.forName("org.xml.sax.InputSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getByteStream"), "method getByteStream");
        check(hasMethod(c, "getCharacterStream"), "method getCharacterStream");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setByteStream"), "method setByteStream");
        check(hasMethod(c, "setCharacterStream"), "method setCharacterStream");
        check(hasMethod(c, "setEncoding"), "method setEncoding");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setSystemId"), "method setSystemId");

        org.xml.sax.InputSource s = new org.xml.sax.InputSource();
        s.setPublicId("p");
        s.setSystemId("s");
        check("p".equals(s.getPublicId()), "publicId");
        check("s".equals(s.getSystemId()), "systemId");
        check(!s.isEmpty(), "not empty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.InputSource is = new org.xml.sax.InputSource();
        check(is.getPublicId() == null && is.getSystemId() == null, "no-arg empty");
        is.setPublicId("pub"); is.setSystemId("sys"); is.setEncoding("UTF-8");
        check("pub".equals(is.getPublicId()) && "sys".equals(is.getSystemId()) && "UTF-8".equals(is.getEncoding()), "setters");
        java.io.ByteArrayInputStream bs = new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8"));
        is.setByteStream(bs);
        check(is.getByteStream() == bs && is.getCharacterStream() == null, "byte stream round-trip");
        check(!is.isEmpty(), "isEmpty false with byte stream");
        java.io.StringReader sr = new java.io.StringReader("<r/>");
        org.xml.sax.InputSource is2 = new org.xml.sax.InputSource(sr);
        check(is2.getCharacterStream() == sr, "reader ctor");
        org.xml.sax.InputSource is3 = new org.xml.sax.InputSource("sys");
        check("sys".equals(is3.getSystemId()), "systemId ctor");
        org.xml.sax.InputSource is4 = new org.xml.sax.InputSource(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        check(is4.getByteStream() != null, "stream ctor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
