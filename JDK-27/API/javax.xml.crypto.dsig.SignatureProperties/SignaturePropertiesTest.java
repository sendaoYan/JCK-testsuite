/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureProperties conformance: white-box structure + black-box behavior.
 * @run main SignaturePropertiesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SignaturePropertiesTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureProperties");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getProperties"), "method getProperties");
        check(hasField(c, "TYPE"), "field TYPE");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.SignatureProperties.TYPE.equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties"), "TYPE");
        javax.xml.crypto.dsig.SignatureProperties a = new javax.xml.crypto.dsig.SignatureProperties(){
            public java.lang.String getId() { return "s"; }
            public java.util.List<javax.xml.crypto.dsig.SignatureProperty> getProperties() { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of SignatureProperties");
        check("s".equals(a.getId()), "getId() returns fixed default");
        check(a.getProperties() == null, "getProperties() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
