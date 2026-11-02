/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMSignContext conformance: API type verification.
 * @run main DOMSignContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMSignContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.dom.DOMSignContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dom.DOMCryptoContext"), "superclass javax.xml.crypto.dom.DOMCryptoContext");
        check(hasInterface(c, "javax.xml.crypto.dsig.XMLSignContext"), "interface javax.xml.crypto.dsig.XMLSignContext");
        check(hasMethod(c, "getNextSibling"), "method getNextSibling");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "setNextSibling"), "method setNextSibling");
        check(hasMethod(c, "setParent"), "method setParent");

        java.security.Key k = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate();
        org.w3c.dom.Node n = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("a");
        javax.xml.crypto.dsig.dom.DOMSignContext sc = new javax.xml.crypto.dsig.dom.DOMSignContext(k, n);
        check(sc.getParent() == n, "parent node");
        sc.setParent(n);
        check(sc.getParent() == n, "setParent");
        check(sc.getNextSibling() == null, "nextSibling null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
