/*
 * @test
 * @summary javax.xml.crypto.dsig.Transform conformance: API type verification.
 * @run main TransformTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.Transform");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasInterface(c, "javax.xml.crypto.AlgorithmMethod"), "interface javax.xml.crypto.AlgorithmMethod");
        check(hasMethod(c, "getParameterSpec"), "method getParameterSpec");
        check(hasMethod(c, "transform"), "method transform");
        check(hasField(c, "BASE64"), "field BASE64");
        check(hasField(c, "ENVELOPED"), "field ENVELOPED");
        check(hasField(c, "XPATH"), "field XPATH");
        check(hasField(c, "XPATH2"), "field XPATH2");
        check(hasField(c, "XSLT"), "field XSLT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
