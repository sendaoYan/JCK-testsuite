/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.ExcC14NParameterSpec conformance: white-box structure + black-box behavior.
 * @run main ExcC14NParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ExcC14NParameterSpecTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.ExcC14NParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.C14NMethodParameterSpec"), "interface javax.xml.crypto.dsig.spec.C14NMethodParameterSpec");
        check(hasMethod(c, "getPrefixList"), "method getPrefixList");
        check(hasField(c, "DEFAULT"), "field DEFAULT");

        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec s = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec();
        check(s.getPrefixList() != null, "getPrefixList default");
        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec s2 = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(java.util.Arrays.asList("a","b"));
        check(s2.getPrefixList().size() == 2, "prefix list size");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("#default".equals(javax.xml.crypto.dsig.spec.ExcC14NParameterSpec.DEFAULT), "DEFAULT constant");
        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec p = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec();
        check(p.getPrefixList() != null && p.getPrefixList().isEmpty(), "no-arg ctor empty prefix list");
        javax.xml.crypto.dsig.spec.ExcC14NParameterSpec p2 = new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(
            java.util.Arrays.asList("foo", "bar"));
        check(java.util.Arrays.asList("foo", "bar").equals(p2.getPrefixList()), "list ctor round-trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
