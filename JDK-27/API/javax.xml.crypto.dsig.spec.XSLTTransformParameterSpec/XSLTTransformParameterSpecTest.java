/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec conformance: API type verification.
 * @run main XSLTTransformParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XSLTTransformParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.TransformParameterSpec"), "interface javax.xml.crypto.dsig.spec.TransformParameterSpec");
        check(hasMethod(c, "getStylesheet"), "method getStylesheet");

        org.w3c.dom.Node n = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("x");
        javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec s =
            new javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec(new javax.xml.crypto.dom.DOMStructure(n));
        check(s.getStylesheet() != null, "getStylesheet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
