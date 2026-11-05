/*
 * @test
 * @summary javax.xml.xpath.XPathFactoryConfigurationException conformance: white-box structure + black-box behavior.
 * @run main XPathFactoryConfigurationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFactoryConfigurationExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathFactoryConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.xpath.XPathException"), "superclass javax.xml.xpath.XPathException");

        javax.xml.xpath.XPathFactoryConfigurationException e = new javax.xml.xpath.XPathFactoryConfigurationException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.xpath.XPathFactoryConfigurationException e = new javax.xml.xpath.XPathFactoryConfigurationException("cfg");
        check("cfg".equals(e.getMessage()) && e instanceof javax.xml.xpath.XPathException, "subclass of XPathException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructors();
        testRealTrigger();        System.out.println("PASS (" + passed + " assertions)");
    }


    static void testConstructors() {
        // (String)
        javax.xml.xpath.XPathFactoryConfigurationException e1 =
            new javax.xml.xpath.XPathFactoryConfigurationException("no impl");
        check("no impl".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");
        // (Throwable)
        RuntimeException cause = new RuntimeException("cfg root");
        javax.xml.xpath.XPathFactoryConfigurationException e2 =
            new javax.xml.xpath.XPathFactoryConfigurationException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("cfg root"), "(Throwable) message derived");
        // two constructors only
        check(javax.xml.xpath.XPathFactoryConfigurationException.class.getConstructors().length == 2,
              "exactly two public constructors");
        // it is a checked XPathException subtype
        check(e2 instanceof javax.xml.xpath.XPathException, "is-a XPathException");
    }

    // real trigger: unsupported object model
    static void testRealTrigger() throws Exception {
        boolean saw = false;
        try {
            javax.xml.xpath.XPathFactory.newInstance("http://bogus.object.model");
        } catch (javax.xml.xpath.XPathFactoryConfigurationException e) {
            saw = e.getMessage() != null && e.getMessage().contains("http://bogus.object.model");
        }
        check(saw, "newInstance(bogus object model) real trigger naming the model");
    }
}

