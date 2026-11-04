/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyValue conformance: white-box structure + black-box behavior.
 * @run main KeyValueTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class KeyValueTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.KeyValue");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getPublicKey"), "method getPublicKey");
        check(hasField(c, "DSA_TYPE"), "field DSA_TYPE");
        check(hasField(c, "EC_TYPE"), "field EC_TYPE");
        check(hasField(c, "RSA_TYPE"), "field RSA_TYPE");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.crypto.dsig.keyinfo.KeyValue.DSA_TYPE.equals("http://www.w3.org/2000/09/xmldsig#DSAKeyValue"), "DSA_TYPE");
        check(javax.xml.crypto.dsig.keyinfo.KeyValue.RSA_TYPE.equals("http://www.w3.org/2000/09/xmldsig#RSAKeyValue"), "RSA_TYPE");
        check(javax.xml.crypto.dsig.keyinfo.KeyValue.EC_TYPE.equals("http://www.w3.org/2009/xmldsig11#ECKeyValue"), "EC_TYPE");
        javax.xml.crypto.dsig.keyinfo.KeyValue a = new javax.xml.crypto.dsig.keyinfo.KeyValue(){
            public java.security.PublicKey getPublicKey() { return null; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of KeyValue");
        check(a.getPublicKey() == null, "getPublicKey() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
