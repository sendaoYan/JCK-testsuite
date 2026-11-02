/*
 * @test
 * @summary javax.xml.parsers.SAXParser conformance: API type verification.
 * @run main SAXParserTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXParserTest {
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
        Class<?> c = Class.forName("javax.xml.parsers.SAXParser");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getParser"), "method getParser");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "getXMLReader"), "method getXMLReader");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setProperty"), "method setProperty");

        javax.xml.parsers.SAXParser p = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
        check(p.getXMLReader() != null, "getXMLReader");
        check(p.getParser() != null, "getParser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
