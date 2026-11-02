/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyInfoFactory conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
