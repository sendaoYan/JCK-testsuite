/*
 * @test
 * @summary javax.xml.crypto.dsig.CanonicalizationMethod conformance: API type verification.
 * @run main CanonicalizationMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CanonicalizationMethodTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.CanonicalizationMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.dsig.Transform"), "interface javax.xml.crypto.dsig.Transform");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasField(c, "EXCLUSIVE"), "field EXCLUSIVE");
        check(hasField(c, "EXCLUSIVE_WITH_COMMENTS"), "field EXCLUSIVE_WITH_COMMENTS");
        check(hasField(c, "INCLUSIVE"), "field INCLUSIVE");
        check(hasField(c, "INCLUSIVE_11"), "field INCLUSIVE_11");
        check(hasField(c, "INCLUSIVE_11_WITH_COMMENTS"), "field INCLUSIVE_11_WITH_COMMENTS");
        check(hasField(c, "INCLUSIVE_WITH_COMMENTS"), "field INCLUSIVE_WITH_COMMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
