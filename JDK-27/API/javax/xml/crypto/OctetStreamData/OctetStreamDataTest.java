/*
 * @test
 * @summary javax.xml.crypto.OctetStreamData conformance: white-box structure + black-box behavior.
 * @run main OctetStreamDataTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class OctetStreamDataTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.OctetStreamData");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.Data"), "interface javax.xml.crypto.Data");
        check(hasMethod(c, "getMimeType"), "method getMimeType");
        check(hasMethod(c, "getOctetStream"), "method getOctetStream");
        check(hasMethod(c, "getURI"), "method getURI");

        javax.xml.crypto.OctetStreamData d = new javax.xml.crypto.OctetStreamData(
            new java.io.ByteArrayInputStream(new byte[]{1,2,3}), "urn:u", "application/octet-stream");
        check("urn:u".equals(d.getURI()), "uri");
        check("application/octet-stream".equals(d.getMimeType()), "mime");
        check(d.getOctetStream() != null, "stream");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.OctetStreamData d = new javax.xml.crypto.OctetStreamData(
            new java.io.ByteArrayInputStream(new byte[]{1,2,3}));
        check(d.getURI() == null, "default URI null");
        check(d.getMimeType() == null, "default mimeType null");
        check(d.getOctetStream() != null, "octet stream non-null");
        javax.xml.crypto.OctetStreamData d2 = new javax.xml.crypto.OctetStreamData(
            new java.io.ByteArrayInputStream(new byte[]{7,8}), "urn:u", "application/octet-stream");
        check("urn:u".equals(d2.getURI()), "URI");
        check("application/octet-stream".equals(d2.getMimeType()), "mimeType");
        java.io.InputStream in = d2.getOctetStream();
        check(in != null && in.read() == 7 && in.read() == 8, "stream bytes readable");
        check(d2.getOctetStream() == in, "getOctetStream stable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
