/*
 * @test
 * @summary javax.xml.datatype.Duration conformance: API type verification.
 * @run main DurationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DurationTest {
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
        Class<?> c = Class.forName("javax.xml.datatype.Duration");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addTo"), "method addTo");
        check(hasMethod(c, "compare"), "method compare");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getDays"), "method getDays");
        check(hasMethod(c, "getField"), "method getField");
        check(hasMethod(c, "getHours"), "method getHours");
        check(hasMethod(c, "getMinutes"), "method getMinutes");
        check(hasMethod(c, "getMonths"), "method getMonths");
        check(hasMethod(c, "getSeconds"), "method getSeconds");
        check(hasMethod(c, "getSign"), "method getSign");
        check(hasMethod(c, "getTimeInMillis"), "method getTimeInMillis");
        check(hasMethod(c, "getXMLSchemaType"), "method getXMLSchemaType");
        check(hasMethod(c, "getYears"), "method getYears");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isLongerThan"), "method isLongerThan");
        check(hasMethod(c, "isSet"), "method isSet");
        check(hasMethod(c, "isShorterThan"), "method isShorterThan");
        check(hasMethod(c, "multiply"), "method multiply");
        check(hasMethod(c, "negate"), "method negate");
        check(hasMethod(c, "normalizeWith"), "method normalizeWith");
        check(hasMethod(c, "subtract"), "method subtract");
        check(hasMethod(c, "toString"), "method toString");

        javax.xml.datatype.DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.Duration d = df.newDuration("P1Y2M3DT4H5M6.789S");
        check(d.getYears() == 1, "years");
        check(d.getMonths() == 2, "months");
        check(d.getDays() == 3, "days");
        check(d.getHours() == 4, "hours");
        check(d.getMinutes() == 5, "minutes");
        check(d.getSeconds() == 6, "seconds");
        check(d.getSign() == 1, "sign");
        check(d.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
