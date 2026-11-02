/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType.Filter conformance: API type verification.
 * @run main FilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class FilterTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathType$Filter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "INTERSECT"), "field INTERSECT");
        check(hasField(c, "SUBTRACT"), "field SUBTRACT");
        check(hasField(c, "UNION"), "field UNION");

        check(javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT != javax.xml.crypto.dsig.spec.XPathType.Filter.UNION, "distinct");
        check(javax.xml.crypto.dsig.spec.XPathType.Filter.SUBTRACT.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
