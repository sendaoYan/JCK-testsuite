/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants.Field conformance: API type verification.
 * @run main FieldTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class FieldTest {
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
        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConstants$Field");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "toString"), "method toString");

        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "YEARS id");
        check(javax.xml.datatype.DatatypeConstants.SECONDS.getId() == 5, "SECONDS id");
        check(javax.xml.datatype.DatatypeConstants.YEARS.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
