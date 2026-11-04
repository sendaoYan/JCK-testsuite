/*
 * @test
 * @summary javax.xml.crypto.dsig.Reference conformance: white-box structure + black-box behavior.
 * @run main ReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ReferenceTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.Reference");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.URIReference"), "interface javax.xml.crypto.URIReference");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getCalculatedDigestValue"), "method getCalculatedDigestValue");
        check(hasMethod(c, "getDereferencedData"), "method getDereferencedData");
        check(hasMethod(c, "getDigestInputStream"), "method getDigestInputStream");
        check(hasMethod(c, "getDigestMethod"), "method getDigestMethod");
        check(hasMethod(c, "getDigestValue"), "method getDigestValue");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getTransforms"), "method getTransforms");
        check(hasMethod(c, "validate"), "method validate");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.dsig.Reference a = new javax.xml.crypto.dsig.Reference(){
            public java.util.List<javax.xml.crypto.dsig.Transform> getTransforms() { return null; }
            public javax.xml.crypto.dsig.DigestMethod getDigestMethod() { return null; }
            public java.lang.String getId() { return "s"; }
            public byte[] getDigestValue() { return null; }
            public byte[] getCalculatedDigestValue() { return null; }
            public boolean validate(javax.xml.crypto.dsig.XMLValidateContext p0) { return true; }
            public javax.xml.crypto.Data getDereferencedData() { return null; }
            public java.io.InputStream getDigestInputStream() { return null; }
            public java.lang.String getURI() { return "s"; }
            public java.lang.String getType() { return "s"; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of Reference");
        check(a.getTransforms() == null, "getTransforms() returns fixed default");
        check(a.getDigestMethod() == null, "getDigestMethod() returns fixed default");
        check("s".equals(a.getId()), "getId() returns fixed default");
        check(a.getDigestValue() == null, "getDigestValue() returns fixed default");
        check(a.getCalculatedDigestValue() == null, "getCalculatedDigestValue() returns fixed default");
        check(a.validate((javax.xml.crypto.dsig.XMLValidateContext) null) == true, "validate() returns fixed default");
        check(a.getDereferencedData() == null, "getDereferencedData() returns fixed default");
        check(a.getDigestInputStream() == null, "getDigestInputStream() returns fixed default");
        check("s".equals(a.getURI()), "getURI() returns fixed default");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
