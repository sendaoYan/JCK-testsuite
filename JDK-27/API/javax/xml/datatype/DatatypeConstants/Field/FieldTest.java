/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants.Field conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConstants$Field");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getId"), "method getId");
        check(hasMethod(c, "toString"), "method toString");

        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "YEARS id");
        check(javax.xml.datatype.DatatypeConstants.SECONDS.getId() == 5, "SECONDS id");
        check(javax.xml.datatype.DatatypeConstants.YEARS.toString() != null, "toString");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(javax.xml.datatype.DatatypeConstants.YEARS instanceof javax.xml.datatype.DatatypeConstants.Field, "YEARS is Field");
        check(javax.xml.datatype.DatatypeConstants.MONTHS instanceof javax.xml.datatype.DatatypeConstants.Field, "MONTHS is Field");
        check(javax.xml.datatype.DatatypeConstants.DAYS instanceof javax.xml.datatype.DatatypeConstants.Field, "DAYS is Field");
        check(javax.xml.datatype.DatatypeConstants.HOURS instanceof javax.xml.datatype.DatatypeConstants.Field, "HOURS is Field");
        check(javax.xml.datatype.DatatypeConstants.MINUTES instanceof javax.xml.datatype.DatatypeConstants.Field, "MINUTES is Field");
        check(javax.xml.datatype.DatatypeConstants.SECONDS instanceof javax.xml.datatype.DatatypeConstants.Field, "SECONDS is Field");
        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "YEARS id 0");
        check(javax.xml.datatype.DatatypeConstants.SECONDS.getId() == 5, "SECONDS id 5");
        check("YEARS".equals(javax.xml.datatype.DatatypeConstants.YEARS.toString()), "toString");
        check(javax.xml.datatype.DatatypeConstants.YEARS != javax.xml.datatype.DatatypeConstants.MONTHS, "distinct fields");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
