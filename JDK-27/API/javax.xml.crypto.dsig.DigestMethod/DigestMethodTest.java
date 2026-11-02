/*
 * @test
 * @summary javax.xml.crypto.dsig.DigestMethod conformance: API type verification.
 * @run main DigestMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DigestMethodTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.DigestMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasInterface(c, "javax.xml.crypto.AlgorithmMethod"), "interface javax.xml.crypto.AlgorithmMethod");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasField(c, "RIPEMD160"), "field RIPEMD160");
        check(hasField(c, "SHA1"), "field SHA1");
        check(hasField(c, "SHA224"), "field SHA224");
        check(hasField(c, "SHA256"), "field SHA256");
        check(hasField(c, "SHA384"), "field SHA384");
        check(hasField(c, "SHA3_224"), "field SHA3_224");
        check(hasField(c, "SHA3_256"), "field SHA3_256");
        check(hasField(c, "SHA3_384"), "field SHA3_384");
        check(hasField(c, "SHA3_512"), "field SHA3_512");
        check(hasField(c, "SHA512"), "field SHA512");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
