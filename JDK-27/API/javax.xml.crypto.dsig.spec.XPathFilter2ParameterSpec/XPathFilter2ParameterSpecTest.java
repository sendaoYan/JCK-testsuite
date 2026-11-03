/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main XPathFilter2ParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFilter2ParameterSpecTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.TransformParameterSpec"), "interface javax.xml.crypto.dsig.spec.TransformParameterSpec");
        check(hasMethod(c, "getXPathList"), "method getXPathList");

        javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec s =
            new javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec(
                java.util.Collections.singletonList(new javax.xml.crypto.dsig.spec.XPathType("//x", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT)));
        check(s.getXPathList().size() == 1, "getXPathList");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.spec.XPathType xt = new javax.xml.crypto.dsig.spec.XPathType(
            "//a", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT);
        javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec p =
            new javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec(java.util.Arrays.asList(xt));
        check(p.getXPathList() != null && p.getXPathList().size() == 1, "xpath list size");
        check(p.getXPathList().get(0) == xt, "xpath list element");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
