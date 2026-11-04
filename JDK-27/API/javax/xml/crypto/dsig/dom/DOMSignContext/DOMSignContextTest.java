/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMSignContext conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.security.KeyPair kp = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        org.w3c.dom.Element next = doc.createElement("b");
        javax.xml.crypto.dsig.dom.DOMSignContext sc = new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), el);
        check(sc.getParent() == el, "parent node");
        check(sc.getNextSibling() == null, "nextSibling default null");
        sc.setParent(next);
        check(sc.getParent() == next, "setParent");
        sc.setNextSibling(el);
        check(sc.getNextSibling() == el, "setNextSibling");
        javax.xml.crypto.dsig.dom.DOMSignContext sc2 = new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), el, next);
        check(sc2.getParent() == el && sc2.getNextSibling() == next, "3-arg Key ctor");
        javax.xml.crypto.KeySelector ks = javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic());
        javax.xml.crypto.dsig.dom.DOMSignContext sc3 = new javax.xml.crypto.dsig.dom.DOMSignContext(ks, el);
        check(sc3.getParent() == el && sc3.getKeySelector() != null, "KeySelector ctor");
        javax.xml.crypto.dsig.dom.DOMSignContext sc4 = new javax.xml.crypto.dsig.dom.DOMSignContext(ks, el, next);
        check(sc4.getParent() == el && sc4.getNextSibling() == next, "3-arg KeySelector ctor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
