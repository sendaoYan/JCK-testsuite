/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilderFactory conformance: API type verification.
 * @run main DocumentBuilderFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DocumentBuilderFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.parsers.DocumentBuilderFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getAttribute"), "method getAttribute");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "isCoalescing"), "method isCoalescing");
        check(hasMethod(c, "isExpandEntityReferences"), "method isExpandEntityReferences");
        check(hasMethod(c, "isIgnoringComments"), "method isIgnoringComments");
        check(hasMethod(c, "isIgnoringElementContentWhitespace"), "method isIgnoringElementContentWhitespace");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newDefaultNSInstance"), "method newDefaultNSInstance");
        check(hasMethod(c, "newDocumentBuilder"), "method newDocumentBuilder");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newNSInstance"), "method newNSInstance");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setCoalescing"), "method setCoalescing");
        check(hasMethod(c, "setExpandEntityReferences"), "method setExpandEntityReferences");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setIgnoringComments"), "method setIgnoringComments");
        check(hasMethod(c, "setIgnoringElementContentWhitespace"), "method setIgnoringElementContentWhitespace");
        check(hasMethod(c, "setNamespaceAware"), "method setNamespaceAware");
        check(hasMethod(c, "setSchema"), "method setSchema");
        check(hasMethod(c, "setValidating"), "method setValidating");
        check(hasMethod(c, "setXIncludeAware"), "method setXIncludeAware");

        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        check(f.isNamespaceAware(), "namespaceAware");
        javax.xml.parsers.DocumentBuilder b = f.newDocumentBuilder();
        check(b != null, "newDocumentBuilder");
        check(b.newDocument() != null, "newDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
