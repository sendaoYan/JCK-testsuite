/*
 * @test
 * @summary javax.xml.crypto.dsig.Transform conformance: white-box structure + black-box behavior.
 * @run main TransformTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.Transform");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasInterface(c, "javax.xml.crypto.AlgorithmMethod"), "interface javax.xml.crypto.AlgorithmMethod");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasMethod(c, "transform"), "method transform");
        check(hasField(c, "BASE64"), "field BASE64");
        check(hasField(c, "ENVELOPED"), "field ENVELOPED");
        check(hasField(c, "XPATH"), "field XPATH");
        check(hasField(c, "XPATH2"), "field XPATH2");
        check(hasField(c, "XSLT"), "field XSLT");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.Transform.BASE64.equals("http://www.w3.org/2000/09/xmldsig#base64"), "BASE64");
        check(javax.xml.crypto.dsig.Transform.ENVELOPED.equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature"), "ENVELOPED");
        check(javax.xml.crypto.dsig.Transform.XPATH.equals("http://www.w3.org/TR/1999/REC-xpath-19991116"), "XPATH");
        check(javax.xml.crypto.dsig.Transform.XPATH2.equals("http://www.w3.org/2002/06/xmldsig-filter2"), "XPATH2");
        check(javax.xml.crypto.dsig.Transform.XSLT.equals("http://www.w3.org/TR/1999/REC-xslt-19991116"), "XSLT");
        javax.xml.crypto.dsig.Transform a = new javax.xml.crypto.dsig.Transform(){
            public java.security.spec.AlgorithmParameterSpec getParameterSpec() { return null; }
            public javax.xml.crypto.Data transform(javax.xml.crypto.Data p0, javax.xml.crypto.XMLCryptoContext p1) { return null; }
            public javax.xml.crypto.Data transform(javax.xml.crypto.Data p0, javax.xml.crypto.XMLCryptoContext p1, java.io.OutputStream p2) { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
            public java.lang.String getAlgorithm() { return "s"; }
        };
        check(a != null, "anon impl of Transform");
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
