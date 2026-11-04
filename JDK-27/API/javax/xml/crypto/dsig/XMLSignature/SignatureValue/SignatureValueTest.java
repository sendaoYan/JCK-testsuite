/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignature.SignatureValue conformance: white-box structure + black-box behavior.
 * @run main SignatureValueTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SignatureValueTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignature$SignatureValue");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "validate"), "method validate");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.dsig.XMLSignature.SignatureValue a = new javax.xml.crypto.dsig.XMLSignature.SignatureValue(){
            public java.lang.String getId() { return "s"; }
            public byte[] getValue() { return null; }
            public boolean validate(javax.xml.crypto.dsig.XMLValidateContext p0) { return true; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of SignatureValue");
        check("s".equals(a.getId()), "getId() returns fixed default");
        check(a.getValue() == null, "getValue() returns fixed default");
        check(a.validate((javax.xml.crypto.dsig.XMLValidateContext) null) == true, "validate() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
