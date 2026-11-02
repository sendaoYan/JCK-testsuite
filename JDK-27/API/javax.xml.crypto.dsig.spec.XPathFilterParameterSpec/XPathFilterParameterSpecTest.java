/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilterParameterSpec conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
