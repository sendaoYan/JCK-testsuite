/*
 * @test
 * @summary javax.xml.crypto.KeySelector.Purpose conformance: white-box structure + black-box behavior.
 * @run main PurposeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class PurposeTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.KeySelector$Purpose");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "DECRYPT"), "field DECRYPT");
        check(hasField(c, "ENCRYPT"), "field ENCRYPT");
        check(hasField(c, "SIGN"), "field SIGN");
        check(hasField(c, "VERIFY"), "field VERIFY");

        check(javax.xml.crypto.KeySelector.Purpose.SIGN != javax.xml.crypto.KeySelector.Purpose.VERIFY, "distinct");
        check(javax.xml.crypto.KeySelector.Purpose.ENCRYPT.toString() != null, "toString");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("sign".equals(javax.xml.crypto.KeySelector.Purpose.SIGN.toString()), "SIGN");
        check("verify".equals(javax.xml.crypto.KeySelector.Purpose.VERIFY.toString()), "VERIFY");
        check("encrypt".equals(javax.xml.crypto.KeySelector.Purpose.ENCRYPT.toString()), "ENCRYPT");
        check("decrypt".equals(javax.xml.crypto.KeySelector.Purpose.DECRYPT.toString()), "DECRYPT");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
