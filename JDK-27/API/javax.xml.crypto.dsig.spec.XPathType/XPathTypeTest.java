/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
