/*
 * @test
 * @summary javax.xml.crypto.dsig.CanonicalizationMethod conformance: white-box structure + black-box behavior.
 * @run main CanonicalizationMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CanonicalizationMethodTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.CanonicalizationMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.dsig.Transform"), "interface javax.xml.crypto.dsig.Transform");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasField(c, "EXCLUSIVE"), "field EXCLUSIVE");
        check(hasField(c, "EXCLUSIVE_WITH_COMMENTS"), "field EXCLUSIVE_WITH_COMMENTS");
        check(hasField(c, "INCLUSIVE"), "field INCLUSIVE");
        check(hasField(c, "INCLUSIVE_11"), "field INCLUSIVE_11");
        check(hasField(c, "INCLUSIVE_11_WITH_COMMENTS"), "field INCLUSIVE_11_WITH_COMMENTS");
        check(hasField(c, "INCLUSIVE_WITH_COMMENTS"), "field INCLUSIVE_WITH_COMMENTS");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"), "INCLUSIVE");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"), "INCLUSIVE_WITH_COMMENTS");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE.equals("http://www.w3.org/2001/10/xml-exc-c14n#"), "EXCLUSIVE");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments"), "EXCLUSIVE_WITH_COMMENTS");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE_11.equals("http://www.w3.org/2006/12/xml-c14n11"), "INCLUSIVE_11");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS.equals("http://www.w3.org/2006/12/xml-c14n11#WithComments"), "INCLUSIVE_11_WITH_COMMENTS");
        javax.xml.crypto.dsig.CanonicalizationMethod a = new javax.xml.crypto.dsig.CanonicalizationMethod(){
            public java.security.spec.AlgorithmParameterSpec getParameterSpec() { return null; }
            public javax.xml.crypto.Data transform(javax.xml.crypto.Data p0, javax.xml.crypto.XMLCryptoContext p1) { return null; }
            public javax.xml.crypto.Data transform(javax.xml.crypto.Data p0, javax.xml.crypto.XMLCryptoContext p1, java.io.OutputStream p2) { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
            public java.lang.String getAlgorithm() { return "s"; }
        };
        check(a != null, "anon impl of CanonicalizationMethod");
        check(a.getParameterSpec() == null, "getParameterSpec() returns fixed default");
        check(a.transform((javax.xml.crypto.Data) null, (javax.xml.crypto.XMLCryptoContext) null) == null, "transform() returns fixed default");
        check(a.transform((javax.xml.crypto.Data) null, (javax.xml.crypto.XMLCryptoContext) null, (java.io.OutputStream) null) == null, "transform() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
        check("s".equals(a.getAlgorithm()), "getAlgorithm() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
