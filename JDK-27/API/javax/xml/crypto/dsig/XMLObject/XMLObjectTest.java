/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLObject conformance: white-box structure + black-box behavior.
 * @run main XMLObjectTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLObjectTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLObject");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getContent"), "method getContent");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getMimeType"), "method getMimeType");
        check(hasField(c, "TYPE"), "field TYPE");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.XMLObject.TYPE.equals("http://www.w3.org/2000/09/xmldsig#Object"), "TYPE");
        javax.xml.crypto.dsig.XMLObject a = new javax.xml.crypto.dsig.XMLObject(){
            public java.util.List<javax.xml.crypto.XMLStructure> getContent() { return null; }
            public java.lang.String getId() { return "s"; }
            public java.lang.String getMimeType() { return "s"; }
            public java.lang.String getEncoding() { return "s"; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of XMLObject");
        check(a.getContent() == null, "getContent() returns fixed default");
        check("s".equals(a.getId()), "getId() returns fixed default");
        check("s".equals(a.getMimeType()), "getMimeType() returns fixed default");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
