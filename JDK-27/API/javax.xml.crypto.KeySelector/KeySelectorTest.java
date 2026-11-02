/*
 * @test
 * @summary javax.xml.crypto.KeySelector conformance: API type verification.
 * @run main KeySelectorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class KeySelectorTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.KeySelector");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "singletonKeySelector"), "method singletonKeySelector");

        java.security.Key k = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate();
        javax.xml.crypto.KeySelector ks = javax.xml.crypto.KeySelector.singletonKeySelector(k);
        check(ks != null, "singletonKeySelector");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
