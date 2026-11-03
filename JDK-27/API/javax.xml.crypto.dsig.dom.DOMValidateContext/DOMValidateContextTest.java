/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMValidateContext conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.security.KeyPair kp = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        javax.xml.crypto.dsig.dom.DOMValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(kp.getPublic(), el);
        check(vc.getNode() == el, "Key ctor node");
        org.w3c.dom.Element el2 = doc.createElement("b");
        vc.setNode(el2);
        check(vc.getNode() == el2, "setNode/getNode");
        javax.xml.crypto.KeySelector ks = javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic());
        javax.xml.crypto.dsig.dom.DOMValidateContext vc2 = new javax.xml.crypto.dsig.dom.DOMValidateContext(ks, el);
        check(vc2.getNode() == el, "KeySelector ctor node");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
