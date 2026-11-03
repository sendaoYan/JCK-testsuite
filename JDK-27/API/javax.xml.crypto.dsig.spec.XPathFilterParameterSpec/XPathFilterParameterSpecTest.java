/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilterParameterSpec conformance: white-box structure + black-box behavior.
 * @run main XPathFilterParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFilterParameterSpecTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathFilterParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.TransformParameterSpec"), "interface javax.xml.crypto.dsig.spec.TransformParameterSpec");
        check(hasMethod(c, "getNamespaceMap"), "method getNamespaceMap");
        check(hasMethod(c, "getXPath"), "method getXPath");

        javax.xml.crypto.dsig.spec.XPathFilterParameterSpec s = new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//x");
        check("//x".equals(s.getXPath()), "getXPath");
        check(s.getNamespaceMap() != null, "getNamespaceMap");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.spec.XPathFilterParameterSpec p = new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//a");
        check("//a".equals(p.getXPath()), "xpath");
        check(p.getNamespaceMap() != null && p.getNamespaceMap().isEmpty(), "default namespace map empty");
        java.util.Map<java.lang.String, java.lang.String> ns = new java.util.HashMap<>();
        ns.put("x", "urn:x");
        javax.xml.crypto.dsig.spec.XPathFilterParameterSpec p2 = new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//x:a", ns);
        check("//x:a".equals(p2.getXPath()), "xpath 2");
        check("urn:x".equals(p2.getNamespaceMap().get("x")), "namespace map");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
