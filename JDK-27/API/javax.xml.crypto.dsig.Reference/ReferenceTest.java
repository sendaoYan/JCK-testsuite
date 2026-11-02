/*
 * @test
 * @summary javax.xml.crypto.dsig.Reference conformance: API type verification.
 * @run main ReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.Reference");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.URIReference"), "interface javax.xml.crypto.URIReference");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getCalculatedDigestValue"), "method getCalculatedDigestValue");
        check(hasMethod(c, "getDereferencedData"), "method getDereferencedData");
        check(hasMethod(c, "getDigestInputStream"), "method getDigestInputStream");
        check(hasMethod(c, "getDigestMethod"), "method getDigestMethod");
        check(hasMethod(c, "getDigestValue"), "method getDigestValue");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getTransforms"), "method getTransforms");
        check(hasMethod(c, "validate"), "method validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
