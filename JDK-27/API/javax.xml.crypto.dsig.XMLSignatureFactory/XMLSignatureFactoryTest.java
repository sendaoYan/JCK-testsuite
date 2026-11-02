/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignatureFactory conformance: API type verification.
 * @run main XMLSignatureFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLSignatureFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignatureFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getKeyInfoFactory"), "method getKeyInfoFactory");
        check(hasMethod(c, "getMechanismType"), "method getMechanismType");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "getURIDereferencer"), "method getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "method isFeatureSupported");
        check(hasMethod(c, "newCanonicalizationMethod"), "method newCanonicalizationMethod");
        check(hasMethod(c, "newDigestMethod"), "method newDigestMethod");
        check(hasMethod(c, "newManifest"), "method newManifest");
        check(hasMethod(c, "newReference"), "method newReference");
        check(hasMethod(c, "newSignatureMethod"), "method newSignatureMethod");
        check(hasMethod(c, "newSignatureProperties"), "method newSignatureProperties");
        check(hasMethod(c, "newSignatureProperty"), "method newSignatureProperty");
        check(hasMethod(c, "newSignedInfo"), "method newSignedInfo");
        check(hasMethod(c, "newTransform"), "method newTransform");
        check(hasMethod(c, "newXMLObject"), "method newXMLObject");
        check(hasMethod(c, "newXMLSignature"), "method newXMLSignature");
        check(hasMethod(c, "unmarshalXMLSignature"), "method unmarshalXMLSignature");

        javax.xml.crypto.dsig.XMLSignatureFactory f = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        check("DOM".equals(f.getMechanismType()), "mechanism");
        check(f.getProvider() != null, "provider");
        check(f.getKeyInfoFactory() != null, "keyInfoFactory");
        javax.xml.crypto.dsig.DigestMethod dm = f.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dm.getAlgorithm()), "digestMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
