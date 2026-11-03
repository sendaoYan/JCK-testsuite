/*
 * @test
 * @summary javax.xml.validation.SchemaFactoryConfigurationError conformance: white-box structure + black-box behavior.
 * @run main SchemaFactoryConfigurationErrorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SchemaFactoryConfigurationErrorTest {
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

        Class<?> c = Class.forName("javax.xml.validation.SchemaFactoryConfigurationError");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");

        javax.xml.validation.SchemaFactoryConfigurationError e = new javax.xml.validation.SchemaFactoryConfigurationError("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.validation.SchemaFactoryConfigurationError e = new javax.xml.validation.SchemaFactoryConfigurationError("m");
        check("m".equals(e.getMessage()), "message ctor");
        java.lang.Throwable c = new RuntimeException("c");
        javax.xml.validation.SchemaFactoryConfigurationError e2 = new javax.xml.validation.SchemaFactoryConfigurationError("m", c);
        check("m".equals(e2.getMessage()) && e2.getCause() == c, "message + throwable");
        javax.xml.validation.SchemaFactoryConfigurationError e3 = new javax.xml.validation.SchemaFactoryConfigurationError(c);
        check(e3.getCause() == c, "throwable ctor");
        javax.xml.validation.SchemaFactoryConfigurationError e4 = new javax.xml.validation.SchemaFactoryConfigurationError();
        check(e4.getMessage() == null, "no-arg");
        check(e instanceof java.lang.Error, "is Error");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
