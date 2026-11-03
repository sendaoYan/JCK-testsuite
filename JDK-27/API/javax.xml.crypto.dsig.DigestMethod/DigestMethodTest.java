/*
 * @test
 * @summary javax.xml.crypto.dsig.DigestMethod conformance: white-box structure + black-box behavior.
 * @run main DigestMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DigestMethodTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.DigestMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasInterface(c, "javax.xml.crypto.AlgorithmMethod"), "interface javax.xml.crypto.AlgorithmMethod");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasField(c, "RIPEMD160"), "field RIPEMD160");
        check(hasField(c, "SHA1"), "field SHA1");
        check(hasField(c, "SHA224"), "field SHA224");
        check(hasField(c, "SHA256"), "field SHA256");
        check(hasField(c, "SHA384"), "field SHA384");
        check(hasField(c, "SHA3_224"), "field SHA3_224");
        check(hasField(c, "SHA3_256"), "field SHA3_256");
        check(hasField(c, "SHA3_384"), "field SHA3_384");
        check(hasField(c, "SHA3_512"), "field SHA3_512");
        check(hasField(c, "SHA512"), "field SHA512");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.DigestMethod.SHA1.equals("http://www.w3.org/2000/09/xmldsig#sha1"), "SHA1");
        check(javax.xml.crypto.dsig.DigestMethod.SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#sha224"), "SHA224");
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals("http://www.w3.org/2001/04/xmlenc#sha256"), "SHA256");
        check(javax.xml.crypto.dsig.DigestMethod.SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#sha384"), "SHA384");
        check(javax.xml.crypto.dsig.DigestMethod.SHA512.equals("http://www.w3.org/2001/04/xmlenc#sha512"), "SHA512");
        check(javax.xml.crypto.dsig.DigestMethod.RIPEMD160.equals("http://www.w3.org/2001/04/xmlenc#ripemd160"), "RIPEMD160");
        check(javax.xml.crypto.dsig.DigestMethod.SHA3_224.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-224"), "SHA3_224");
        check(javax.xml.crypto.dsig.DigestMethod.SHA3_256.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-256"), "SHA3_256");
        check(javax.xml.crypto.dsig.DigestMethod.SHA3_384.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-384"), "SHA3_384");
        check(javax.xml.crypto.dsig.DigestMethod.SHA3_512.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-512"), "SHA3_512");
        javax.xml.crypto.dsig.DigestMethod a = new javax.xml.crypto.dsig.DigestMethod(){
            public java.security.spec.AlgorithmParameterSpec getParameterSpec() { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
            public java.lang.String getAlgorithm() { return "s"; }
        };
        check(a != null, "anon impl of DigestMethod");
        check(a.getParameterSpec() == null, "getParameterSpec() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
        check("s".equals(a.getAlgorithm()), "getAlgorithm() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
