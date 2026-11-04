/*
 * @test
 * @summary javax.xml.datatype.Duration conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.datatype.DatatypeFactory f = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.Duration d = f.newDuration("P1Y2M3DT4H5M6S");
        check(d.getYears() == 1 && d.getMonths() == 2 && d.getDays() == 3, "date fields");
        check(d.getHours() == 4 && d.getMinutes() == 5 && d.getSeconds() == 6, "time fields");
        check(d.isSet(javax.xml.datatype.DatatypeConstants.YEARS), "YEARS set");
        check(((Number) d.getField(javax.xml.datatype.DatatypeConstants.YEARS)).intValue() == 1, "getField YEARS");
        check(d.getSign() == 1, "sign positive");
        javax.xml.datatype.Duration neg = d.negate();
        check(neg.getSign() == -1 && neg.getYears() == 1, "negate");
        javax.xml.datatype.Duration two = d.multiply(2);
        check(two.getYears() == 2 && two.getHours() == 8, "multiply(int)");
        javax.xml.datatype.Duration sum = d.add(d);
        check(sum.getYears() == 2 && sum.getSeconds() == 12, "add");
        javax.xml.datatype.Duration diff = sum.subtract(d);
        check(diff.getYears() == 1 && diff.getSeconds() == 6, "subtract");
        check(d.compare(d) == 0, "compare equal");
        check(d.isLongerThan(neg), "isLongerThan");
        check(neg.isShorterThan(d), "isShorterThan");
        check(d.equals(f.newDuration("P1Y2M3DT4H5M6S")), "equals");
        check(d.hashCode() == f.newDuration("P1Y2M3DT4H5M6S").hashCode(), "hashCode consistent");
        check(d.getXMLSchemaType().equals(javax.xml.datatype.DatatypeConstants.DURATION), "schema type duration");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
