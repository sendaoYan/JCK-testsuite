/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilder conformance: API type verification.
 * @run main DocumentBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DocumentBuilderTest {
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
        Class<?> c = Class.forName("javax.xml.parsers.DocumentBuilder");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getDOMImplementation"), "method getDOMImplementation");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "newDocument"), "method newDocument");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");

        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        check(d != null, "newDocument");
        check(b.getDOMImplementation() != null, "getDOMImplementation");
        check(b.newDocument().getDocumentElement() == null, "empty document");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
