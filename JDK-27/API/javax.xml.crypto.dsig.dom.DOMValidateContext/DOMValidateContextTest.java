/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMValidateContext conformance: API type verification.
 * @run main DOMValidateContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMValidateContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.dom.DOMValidateContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dom.DOMCryptoContext"), "superclass javax.xml.crypto.dom.DOMCryptoContext");
        check(hasInterface(c, "javax.xml.crypto.dsig.XMLValidateContext"), "interface javax.xml.crypto.dsig.XMLValidateContext");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "setNode"), "method setNode");

        java.security.Key k = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        org.w3c.dom.Node n = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("a");
        javax.xml.crypto.dsig.dom.DOMValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(k, n);
        check(vc.getNode() == n, "getNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
