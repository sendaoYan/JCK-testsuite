/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureProperty conformance: API type verification.
 * @run main SignaturePropertyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SignaturePropertyTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureProperty");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "getContent"), "method getContent");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "getTarget"), "method getTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
