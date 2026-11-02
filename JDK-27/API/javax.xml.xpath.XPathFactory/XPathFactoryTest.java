/*
 * @test
 * @summary javax.xml.xpath.XPathFactory conformance: API type verification.
 * @run main XPathFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "isObjectModelSupported"), "method isObjectModelSupported");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newXPath"), "method newXPath");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setXPathFunctionResolver"), "method setXPathFunctionResolver");
        check(hasMethod(c, "setXPathVariableResolver"), "method setXPathVariableResolver");
        check(hasField(c, "DEFAULT_OBJECT_MODEL_URI"), "field DEFAULT_OBJECT_MODEL_URI");
        check(hasField(c, "DEFAULT_PROPERTY_NAME"), "field DEFAULT_PROPERTY_NAME");

        javax.xml.xpath.XPathFactory f = javax.xml.xpath.XPathFactory.newInstance();
        check(f.isObjectModelSupported(javax.xml.xpath.XPathFactory.DEFAULT_OBJECT_MODEL_URI), "isObjectModelSupported");
        check(f.newXPath() != null, "newXPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
