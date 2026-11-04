/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType conformance: white-box structure + black-box behavior.
 * @run main XPathTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathTypeTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getExpression"), "method getExpression");
        check(hasMethod(c, "getFilter"), "method getFilter");
        check(hasMethod(c, "getNamespaceMap"), "method getNamespaceMap");

        javax.xml.crypto.dsig.spec.XPathType t = new javax.xml.crypto.dsig.spec.XPathType("//x", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT);
        check("//x".equals(t.getExpression()), "expression");
        check(t.getFilter() == javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT, "filter");
        check(t.getNamespaceMap() != null, "namespaceMap");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.spec.XPathType xt = new javax.xml.crypto.dsig.spec.XPathType(
            "//a", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT);
        check("//a".equals(xt.getExpression()), "expression");
        check(xt.getFilter() == javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT, "filter");
        check(xt.getNamespaceMap() != null && xt.getNamespaceMap().isEmpty(), "default namespace map empty");
        java.util.Map<java.lang.String, java.lang.String> ns = new java.util.HashMap<>();
        ns.put("x", "urn:x");
        javax.xml.crypto.dsig.spec.XPathType xt2 = new javax.xml.crypto.dsig.spec.XPathType(
            "//x:a", javax.xml.crypto.dsig.spec.XPathType.Filter.UNION, ns);
        check("//x:a".equals(xt2.getExpression()) && xt2.getFilter() == javax.xml.crypto.dsig.spec.XPathType.Filter.UNION, "3-arg ctor");
        check("urn:x".equals(xt2.getNamespaceMap().get("x")), "namespace map");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
