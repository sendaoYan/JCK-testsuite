/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.RSAPSSParameterSpec conformance: API type verification.
 * @run main RSAPSSParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class RSAPSSParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.RSAPSSParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec"), "interface javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec");
        check(hasMethod(c, "getPSSParameterSpec"), "method getPSSParameterSpec");

        java.security.spec.PSSParameterSpec pss = new java.security.spec.PSSParameterSpec("SHA-256", "MGF1",
            new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        javax.xml.crypto.dsig.spec.RSAPSSParameterSpec s = new javax.xml.crypto.dsig.spec.RSAPSSParameterSpec(pss);
        check(s.getPSSParameterSpec() == pss, "getPSSParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
