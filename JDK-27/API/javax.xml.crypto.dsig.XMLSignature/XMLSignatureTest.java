/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignature conformance: API type verification.
 * @run main XMLSignatureTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLSignatureTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignature");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getKeyInfo"), "method getKeyInfo");
        check(hasMethod(c, "getKeySelectorResult"), "method getKeySelectorResult");
        check(hasMethod(c, "getObjects"), "method getObjects");
        check(hasMethod(c, "getSignatureValue"), "method getSignatureValue");
        check(hasMethod(c, "getSignedInfo"), "method getSignedInfo");
        check(hasMethod(c, "sign"), "method sign");
        check(hasMethod(c, "validate"), "method validate");
        check(hasField(c, "XMLNS"), "field XMLNS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
