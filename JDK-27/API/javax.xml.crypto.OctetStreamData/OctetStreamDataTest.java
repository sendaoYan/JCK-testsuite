/*
 * @test
 * @summary javax.xml.crypto.OctetStreamData conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
