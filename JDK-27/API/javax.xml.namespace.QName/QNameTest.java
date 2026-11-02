/*
 * @test
 * @summary javax.xml.namespace.QName conformance: API type verification.
 * @run main QNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class QNameTest {
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
        Class<?> c = Class.forName("javax.xml.namespace.QName");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "java.io.Serializable"), "interface java.io.Serializable");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getLocalPart"), "method getLocalPart");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "valueOf"), "method valueOf");

        javax.xml.namespace.QName q = new javax.xml.namespace.QName("http://ns", "local", "p");
        check("http://ns".equals(q.getNamespaceURI()), "ns");
        check("local".equals(q.getLocalPart()), "local");
        check("p".equals(q.getPrefix()), "prefix");
        check(q.equals(new javax.xml.namespace.QName("http://ns", "local")), "equals");
        check(q.hashCode() == new javax.xml.namespace.QName("http://ns", "local").hashCode(), "hashCode");
        check("{http://ns}local".equals(q.toString()), "toString");
        check(javax.xml.namespace.QName.valueOf("{http://ns}local").equals(new javax.xml.namespace.QName("http://ns", "local")), "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
