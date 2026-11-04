/*
 * @test
 * @summary javax.xml.validation.SchemaFactoryLoader conformance: white-box structure + black-box behavior.
 * @run main SchemaFactoryLoaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SchemaFactoryLoaderTest {
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

        Class<?> c = Class.forName("javax.xml.validation.SchemaFactoryLoader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "newFactory"), "method newFactory");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // abstract loader: verify a concrete subclass dispatches newFactory
        final javax.xml.validation.SchemaFactory delegate = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.SchemaFactoryLoader loader = new javax.xml.validation.SchemaFactoryLoader() {
            public javax.xml.validation.SchemaFactory newFactory(String schemaLanguage) {
                return javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(schemaLanguage) ? delegate : null;
            }
        };
        check(loader.newFactory(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI) == delegate, "newFactory(W3C) dispatches");
        check(loader.newFactory("http://example.invalid/lang") == null, "newFactory(unknown) null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
