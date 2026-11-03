/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureMethod conformance: white-box structure + black-box behavior.
 * @run main SignatureMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SignatureMethodTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasInterface(c, "javax.xml.crypto.AlgorithmMethod"), "interface javax.xml.crypto.AlgorithmMethod");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasField(c, "DSA_SHA1"), "field DSA_SHA1");
        check(hasField(c, "DSA_SHA256"), "field DSA_SHA256");
        check(hasField(c, "ECDSA_SHA1"), "field ECDSA_SHA1");
        check(hasField(c, "ECDSA_SHA224"), "field ECDSA_SHA224");
        check(hasField(c, "ECDSA_SHA256"), "field ECDSA_SHA256");
        check(hasField(c, "ECDSA_SHA384"), "field ECDSA_SHA384");
        check(hasField(c, "ECDSA_SHA3_224"), "field ECDSA_SHA3_224");
        check(hasField(c, "ECDSA_SHA3_256"), "field ECDSA_SHA3_256");
        check(hasField(c, "ECDSA_SHA3_384"), "field ECDSA_SHA3_384");
        check(hasField(c, "ECDSA_SHA3_512"), "field ECDSA_SHA3_512");
        check(hasField(c, "ECDSA_SHA512"), "field ECDSA_SHA512");
        check(hasField(c, "ED25519"), "field ED25519");
        check(hasField(c, "ED448"), "field ED448");
        check(hasField(c, "HMAC_SHA1"), "field HMAC_SHA1");
        check(hasField(c, "HMAC_SHA224"), "field HMAC_SHA224");
        check(hasField(c, "HMAC_SHA256"), "field HMAC_SHA256");
        check(hasField(c, "HMAC_SHA384"), "field HMAC_SHA384");
        check(hasField(c, "HMAC_SHA512"), "field HMAC_SHA512");
        check(hasField(c, "RSA_PSS"), "field RSA_PSS");
        check(hasField(c, "RSA_SHA1"), "field RSA_SHA1");
        check(hasField(c, "RSA_SHA224"), "field RSA_SHA224");
        check(hasField(c, "RSA_SHA256"), "field RSA_SHA256");
        check(hasField(c, "RSA_SHA384"), "field RSA_SHA384");
        check(hasField(c, "RSA_SHA512"), "field RSA_SHA512");
        check(hasField(c, "SHA1_RSA_MGF1"), "field SHA1_RSA_MGF1");
        check(hasField(c, "SHA224_RSA_MGF1"), "field SHA224_RSA_MGF1");
        check(hasField(c, "SHA256_RSA_MGF1"), "field SHA256_RSA_MGF1");
        check(hasField(c, "SHA384_RSA_MGF1"), "field SHA384_RSA_MGF1");
        check(hasField(c, "SHA3_224_RSA_MGF1"), "field SHA3_224_RSA_MGF1");
        check(hasField(c, "SHA3_256_RSA_MGF1"), "field SHA3_256_RSA_MGF1");
        check(hasField(c, "SHA3_384_RSA_MGF1"), "field SHA3_384_RSA_MGF1");
        check(hasField(c, "SHA3_512_RSA_MGF1"), "field SHA3_512_RSA_MGF1");
        check(hasField(c, "SHA512_RSA_MGF1"), "field SHA512_RSA_MGF1");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.SignatureMethod.DSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1"), "DSA_SHA1");
        check(javax.xml.crypto.dsig.SignatureMethod.DSA_SHA256.equals("http://www.w3.org/2009/xmldsig11#dsa-sha256"), "DSA_SHA256");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1"), "RSA_SHA1");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha224"), "RSA_SHA224");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"), "RSA_SHA256");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"), "RSA_SHA384");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"), "RSA_SHA512");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA1_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1"), "SHA1_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA224_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha224-rsa-MGF1"), "SHA224_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA256_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1"), "SHA256_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA384_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha384-rsa-MGF1"), "SHA384_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA512_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1"), "SHA512_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA1.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"), "ECDSA_SHA1");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224"), "ECDSA_SHA224");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"), "ECDSA_SHA256");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384"), "ECDSA_SHA384");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"), "ECDSA_SHA512");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA1.equals("http://www.w3.org/2000/09/xmldsig#hmac-sha1"), "HMAC_SHA1");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224"), "HMAC_SHA224");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256"), "HMAC_SHA256");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384"), "HMAC_SHA384");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512"), "HMAC_SHA512");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_PSS.equals("http://www.w3.org/2007/05/xmldsig-more#rsa-pss"), "RSA_PSS");
        check(javax.xml.crypto.dsig.SignatureMethod.ED25519.equals("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519"), "ED25519");
        check(javax.xml.crypto.dsig.SignatureMethod.ED448.equals("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed448"), "ED448");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA3_224_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-224-rsa-MGF1"), "SHA3_224_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA3_256_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-256-rsa-MGF1"), "SHA3_256_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA3_384_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-384-rsa-MGF1"), "SHA3_384_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.SHA3_512_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-512-rsa-MGF1"), "SHA3_512_RSA_MGF1");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA3_224.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-224"), "ECDSA_SHA3_224");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA3_256.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-256"), "ECDSA_SHA3_256");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA3_384.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-384"), "ECDSA_SHA3_384");
        check(javax.xml.crypto.dsig.SignatureMethod.ECDSA_SHA3_512.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-512"), "ECDSA_SHA3_512");
        javax.xml.crypto.dsig.SignatureMethod a = new javax.xml.crypto.dsig.SignatureMethod(){
            public java.security.spec.AlgorithmParameterSpec getParameterSpec() { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
            public java.lang.String getAlgorithm() { return "s"; }
        };
        check(a != null, "anon impl of SignatureMethod");
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
