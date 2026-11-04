/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyInfoFactory conformance: white-box structure + black-box behavior.
 * @run main KeyInfoFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class KeyInfoFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.KeyInfoFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getMechanismType"), "method getMechanismType");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "getURIDereferencer"), "method getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "method isFeatureSupported");
        check(hasMethod(c, "newKeyInfo"), "method newKeyInfo");
        check(hasMethod(c, "newKeyName"), "method newKeyName");
        check(hasMethod(c, "newKeyValue"), "method newKeyValue");
        check(hasMethod(c, "newPGPData"), "method newPGPData");
        check(hasMethod(c, "newRetrievalMethod"), "method newRetrievalMethod");
        check(hasMethod(c, "newX509Data"), "method newX509Data");
        check(hasMethod(c, "newX509IssuerSerial"), "method newX509IssuerSerial");
        check(hasMethod(c, "unmarshalKeyInfo"), "method unmarshalKeyInfo");

        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kf = javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("DOM");
        check("DOM".equals(kf.getMechanismType()), "mechanism");
        check(kf.getProvider() != null, "provider");
        check("x".equals(kf.newKeyName("x").getName()), "newKeyName");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kf = javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance();
        check(kf != null, "getInstance");
        check("DOM".equals(kf.getMechanismType()), "mechanismType DOM");
        check(kf.getProvider() != null, "provider non-null");
        javax.xml.crypto.dsig.keyinfo.KeyName kn = kf.newKeyName("kn");
        check("kn".equals(kn.getName()), "newKeyName");
        java.security.KeyPair kp = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        javax.xml.crypto.dsig.keyinfo.KeyValue kv = kf.newKeyValue(kp.getPublic());
        check(kv.getPublicKey() == kp.getPublic(), "newKeyValue round-trip");
        javax.xml.crypto.dsig.keyinfo.X509IssuerSerial xis = kf.newX509IssuerSerial("CN=Test", new java.math.BigInteger("42"));
        check("CN=Test".equals(xis.getIssuerName()), "X509IssuerSerial issuer");
        check(new java.math.BigInteger("42").equals(xis.getSerialNumber()), "X509IssuerSerial serial");
        javax.xml.crypto.dsig.keyinfo.RetrievalMethod rm = kf.newRetrievalMethod("#rm");
        check("#rm".equals(rm.getURI()), "newRetrievalMethod uri");
        javax.xml.crypto.dsig.keyinfo.X509Data xd = kf.newX509Data(java.util.Arrays.asList("CN=Test"));
        check(xd.getContent().size() == 1 && "CN=Test".equals(xd.getContent().get(0)), "newX509Data");
        javax.xml.crypto.dsig.keyinfo.KeyInfo ki = kf.newKeyInfo(java.util.Collections.singletonList((javax.xml.crypto.XMLStructure) kn));
        check(ki.getContent().size() == 1, "newKeyInfo content");
        check(!kf.isFeatureSupported("x"), "isFeatureSupported false");
        check(kf.getURIDereferencer() != null, "default URIDereferencer non-null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
