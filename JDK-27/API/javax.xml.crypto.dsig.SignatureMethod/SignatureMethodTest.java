/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureMethod conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
