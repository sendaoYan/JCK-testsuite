/*
 * @test
 * @summary javax.xml.stream.FactoryConfigurationError conformance: white-box structure + black-box behavior.
 * @run main FactoryConfigurationErrorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class FactoryConfigurationErrorTest {
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

        Class<?> c = Class.forName("javax.xml.stream.FactoryConfigurationError");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getMessage"), "method getMessage");

        javax.xml.stream.FactoryConfigurationError e = new javax.xml.stream.FactoryConfigurationError(new Exception("x"));
        check("x".equals(e.getException().getMessage()), "getException");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.FactoryConfigurationError e = new javax.xml.stream.FactoryConfigurationError("why");
        check("why".equals(e.getMessage()), "message ctor");
        java.lang.Exception c = new java.io.IOException("io");
        javax.xml.stream.FactoryConfigurationError e2 = new javax.xml.stream.FactoryConfigurationError(c);
        check(e2.getException() == c && e2.getCause() == c, "exception ctor");
        check("io".equals(e2.getMessage()), "message from exception");
        javax.xml.stream.FactoryConfigurationError e3 = new javax.xml.stream.FactoryConfigurationError(c, "extra");
        check("extra".equals(e3.getMessage()) && e3.getException() == c, "(exception, message)");
        javax.xml.stream.FactoryConfigurationError e4 = new javax.xml.stream.FactoryConfigurationError();
        check(e4.getMessage() == null && e4.getException() == null, "no-arg");
        check(e instanceof java.lang.Error, "is Error");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorChain();
        testThrowability();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: all four constructor overloads ----
    static void testConstructorChain() {
        javax.xml.stream.FactoryConfigurationError e1 = new javax.xml.stream.FactoryConfigurationError("why1");
        check("why1".equals(e1.getMessage()), "(String) message");
        check(e1.getException() == null && e1.getCause() == null, "(String) no exception/cause");
        java.lang.Exception io = new java.io.IOException("ioe");
        javax.xml.stream.FactoryConfigurationError e2 = new javax.xml.stream.FactoryConfigurationError(io);
        check(e2.getException() == io, "(Exception) getException identity");
        check(e2.getCause() == io, "(Exception) getCause identity");
        check("ioe".equals(e2.getMessage()), "(Exception) message from exception");
        javax.xml.stream.FactoryConfigurationError e3 = new javax.xml.stream.FactoryConfigurationError(io, "extra");
        check("extra".equals(e3.getMessage()), "(Exception,String) message wins");
        check(e3.getException() == io, "(Exception,String) exception kept");
        javax.xml.stream.FactoryConfigurationError e4 = new javax.xml.stream.FactoryConfigurationError();
        check(e4.getMessage() == null && e4.getException() == null, "no-arg all null");
        // hierarchy and throwability
        check(e4 instanceof java.lang.Error, "is Error");
        check(e4 instanceof java.io.Serializable, "Error is Serializable");
    }

    // ---- Deepened 2: thrown and caught like an Error; factory discovery can raise it ----
    static void testThrowability() {
        boolean caught = false;
        try {
            throw new javax.xml.stream.FactoryConfigurationError("probe");
        } catch (java.lang.Error e) {
            caught = e instanceof javax.xml.stream.FactoryConfigurationError
                && "probe".equals(e.getMessage());
        }
        check(caught, "throwable/catchable as Error with message");
        // invalid factory class via system property -> FactoryConfigurationError, property restored in finally
        String key = "javax.xml.stream.XMLInputFactory";
        String saved = System.getProperty(key);
        try {
            System.setProperty(key, "no.such.Clazz");
            boolean fce = false;
            try {
                javax.xml.stream.XMLInputFactory.newFactory();
            } catch (javax.xml.stream.FactoryConfigurationError e) {
                fce = "no.such.Clazz".equals(String.valueOf(e.getMessage()))
                    || String.valueOf(e.getMessage()).contains("no.such.Clazz")
                    || e.getCause() != null || e.getException() != null;
            }
            check(fce, "unresolvable factory class -> FactoryConfigurationError");
        } finally {
            if (saved == null) System.clearProperty(key); else System.setProperty(key, saved);
        }
        check(System.getProperty(key) == null ? saved == null : saved.equals(System.getProperty(key)),
            "factory property restored after error path");
    }
}
