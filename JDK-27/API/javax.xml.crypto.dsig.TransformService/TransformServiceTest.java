/*
 * @test
 * @summary javax.xml.crypto.dsig.TransformService conformance: white-box structure + black-box behavior.
 * @run main TransformServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformServiceTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.TransformService");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.Transform"), "interface javax.xml.crypto.dsig.Transform");
        check(hasMethod(c, "getAlgorithm"), "method getAlgorithm");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getMechanismType"), "method getMechanismType");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "marshalParams"), "method marshalParams");

        javax.xml.crypto.dsig.TransformService ts = javax.xml.crypto.dsig.TransformService.getInstance(javax.xml.crypto.dsig.Transform.BASE64, "DOM");
        check("DOM".equals(ts.getMechanismType()), "mechanism");
        check(javax.xml.crypto.dsig.Transform.BASE64.equals(ts.getAlgorithm()), "algorithm");
        check(ts.getProvider() != null, "provider");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.TransformService c14n = javax.xml.crypto.dsig.TransformService.getInstance(
            javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, "DOM");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(c14n.getAlgorithm()), "getInstance alg");
        check("DOM".equals(c14n.getMechanismType()), "mechanismType");
        check(c14n.getProvider() != null, "provider");
        c14n.init((javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        check(true, "c14n init(null)");
        javax.xml.crypto.dsig.TransformService b64 = javax.xml.crypto.dsig.TransformService.getInstance(
            javax.xml.crypto.dsig.Transform.BASE64, "DOM");
        check(javax.xml.crypto.dsig.Transform.BASE64.equals(b64.getAlgorithm()), "base64 alg");
        javax.xml.crypto.dsig.TransformService env = javax.xml.crypto.dsig.TransformService.getInstance(
            javax.xml.crypto.dsig.Transform.ENVELOPED, "DOM");
        check(javax.xml.crypto.dsig.Transform.ENVELOPED.equals(env.getAlgorithm()), "enveloped alg");
        javax.xml.crypto.dsig.TransformService xpath = javax.xml.crypto.dsig.TransformService.getInstance(
            javax.xml.crypto.dsig.Transform.XPATH, "DOM");
        xpath.init(new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//a"));
        check(true, "xpath init");
        javax.xml.crypto.dsig.TransformService c14n2 = javax.xml.crypto.dsig.TransformService.getInstance(
            javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, "DOM", "XMLDSig");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(c14n2.getAlgorithm()), "provider-name getInstance");
        java.security.KeyPair kp = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        c14n.marshalParams(new javax.xml.crypto.dom.DOMStructure(el),
            new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), el));
        check(true, "marshalParams no-throw");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
