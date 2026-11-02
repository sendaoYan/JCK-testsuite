/*
 * @test
 * @summary javax.xml.transform.TransformerConfigurationException conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.TransformerConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.transform.TransformerException"), "superclass javax.xml.transform.TransformerException");

        javax.xml.transform.TransformerConfigurationException e =
            new javax.xml.transform.TransformerConfigurationException("m", (javax.xml.transform.SourceLocator) null, new RuntimeException("c"));
        check("m".equals(e.getMessage()), "msg");
        check(e.getCause() != null, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
