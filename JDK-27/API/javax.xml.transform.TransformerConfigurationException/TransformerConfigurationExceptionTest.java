/*
 * @test
 * @summary javax.xml.transform.TransformerConfigurationException conformance: white-box structure + black-box behavior.
 * @run main TransformerConfigurationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerConfigurationExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.transform.TransformerConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.transform.TransformerException"), "superclass javax.xml.transform.TransformerException");

        javax.xml.transform.TransformerConfigurationException e =
            new javax.xml.transform.TransformerConfigurationException("m", (javax.xml.transform.SourceLocator) null, new RuntimeException("c"));
        check("m".equals(e.getMessage()), "msg");
        check(e.getCause() != null, "cause");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.TransformerConfigurationException e = new javax.xml.transform.TransformerConfigurationException("cfg");
        check("cfg".equals(e.getMessage()), "message ctor");
        check(e instanceof javax.xml.transform.TransformerException, "subclass");
        javax.xml.transform.TransformerConfigurationException e2 = new javax.xml.transform.TransformerConfigurationException();
        check("Configuration Error".equals(e2.getMessage()), "no-arg default message");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
