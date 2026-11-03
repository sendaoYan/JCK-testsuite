/*
 * @test
 * @summary javax.xml.crypto.AlgorithmMethod conformance: white-box structure + black-box behavior.
 * @run main AlgorithmMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AlgorithmMethodTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.AlgorithmMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getAlgorithm"), "method getAlgorithm");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.AlgorithmMethod a = new javax.xml.crypto.AlgorithmMethod(){
            public java.lang.String getAlgorithm() { return "s"; }
            public java.security.spec.AlgorithmParameterSpec getParameterSpec() { return null; }
        };
        check(a != null, "anon impl of AlgorithmMethod");
        check("s".equals(a.getAlgorithm()), "getAlgorithm() returns fixed default");
        check(a.getParameterSpec() == null, "getParameterSpec() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
