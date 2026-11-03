/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants conformance: white-box structure + black-box behavior.
 * @run main DatatypeConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DatatypeConstantsTest {
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

        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConstants");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasField(c, "APRIL"), "field APRIL");
        check(hasField(c, "AUGUST"), "field AUGUST");
        check(hasField(c, "DATE"), "field DATE");
        check(hasField(c, "DATETIME"), "field DATETIME");
        check(hasField(c, "DAYS"), "field DAYS");
        check(hasField(c, "DECEMBER"), "field DECEMBER");
        check(hasField(c, "DURATION"), "field DURATION");
        check(hasField(c, "DURATION_DAYTIME"), "field DURATION_DAYTIME");
        check(hasField(c, "DURATION_YEARMONTH"), "field DURATION_YEARMONTH");
        check(hasField(c, "EQUAL"), "field EQUAL");
        check(hasField(c, "FEBRUARY"), "field FEBRUARY");
        check(hasField(c, "FIELD_UNDEFINED"), "field FIELD_UNDEFINED");
        check(hasField(c, "GDAY"), "field GDAY");
        check(hasField(c, "GMONTH"), "field GMONTH");
        check(hasField(c, "GMONTHDAY"), "field GMONTHDAY");
        check(hasField(c, "GREATER"), "field GREATER");
        check(hasField(c, "GYEAR"), "field GYEAR");
        check(hasField(c, "GYEARMONTH"), "field GYEARMONTH");
        check(hasField(c, "HOURS"), "field HOURS");
        check(hasField(c, "INDETERMINATE"), "field INDETERMINATE");
        check(hasField(c, "JANUARY"), "field JANUARY");
        check(hasField(c, "JULY"), "field JULY");
        check(hasField(c, "JUNE"), "field JUNE");
        check(hasField(c, "LESSER"), "field LESSER");
        check(hasField(c, "MARCH"), "field MARCH");
        check(hasField(c, "MAX_TIMEZONE_OFFSET"), "field MAX_TIMEZONE_OFFSET");
        check(hasField(c, "MAY"), "field MAY");
        check(hasField(c, "MINUTES"), "field MINUTES");
        check(hasField(c, "MIN_TIMEZONE_OFFSET"), "field MIN_TIMEZONE_OFFSET");
        check(hasField(c, "MONTHS"), "field MONTHS");
        check(hasField(c, "NOVEMBER"), "field NOVEMBER");
        check(hasField(c, "OCTOBER"), "field OCTOBER");
        check(hasField(c, "SECONDS"), "field SECONDS");
        check(hasField(c, "SEPTEMBER"), "field SEPTEMBER");
        check(hasField(c, "TIME"), "field TIME");
        check(hasField(c, "YEARS"), "field YEARS");

        check(javax.xml.datatype.DatatypeConstants.JANUARY == 1, "JANUARY");
        check(javax.xml.datatype.DatatypeConstants.DECEMBER == 12, "DECEMBER");
        check(javax.xml.datatype.DatatypeConstants.LESSER == -1, "LESSER");
        check(javax.xml.datatype.DatatypeConstants.EQUAL == 0, "EQUAL");
        check(javax.xml.datatype.DatatypeConstants.GREATER == 1, "GREATER");
        check(javax.xml.datatype.DatatypeConstants.INDETERMINATE == 2, "INDETERMINATE");
        check(javax.xml.datatype.DatatypeConstants.DATETIME != null, "DATETIME");
        check(javax.xml.datatype.DatatypeConstants.DURATION != null, "DURATION");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(javax.xml.datatype.DatatypeConstants.JANUARY == 1 && javax.xml.datatype.DatatypeConstants.DECEMBER == 12, "month constants");
        check(javax.xml.datatype.DatatypeConstants.LESSER == -1 && javax.xml.datatype.DatatypeConstants.EQUAL == 0
              && javax.xml.datatype.DatatypeConstants.GREATER == 1 && javax.xml.datatype.DatatypeConstants.INDETERMINATE == 2, "comparison constants");
        check(javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED == -2147483648, "FIELD_UNDEFINED");
        check(javax.xml.datatype.DatatypeConstants.MAX_TIMEZONE_OFFSET == -840, "MAX_TIMEZONE_OFFSET");
        check(javax.xml.datatype.DatatypeConstants.MIN_TIMEZONE_OFFSET == 840, "MIN_TIMEZONE_OFFSET");
        check(javax.xml.datatype.DatatypeConstants.YEARS != null && javax.xml.datatype.DatatypeConstants.SECONDS != null, "Field constants");
        check("YEARS".equals(javax.xml.datatype.DatatypeConstants.YEARS.toString()), "Field toString");
        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "Field YEARS id");
        check(javax.xml.datatype.DatatypeConstants.DURATION.getLocalPart().equals("duration"), "DURATION QName");
        check(javax.xml.datatype.DatatypeConstants.DATETIME.getLocalPart().equals("dateTime"), "DATETIME QName");
        check(javax.xml.datatype.DatatypeConstants.DATE.getLocalPart().equals("date"), "DATE QName");
        check(javax.xml.datatype.DatatypeConstants.TIME.getLocalPart().equals("time"), "TIME QName");
        check(javax.xml.datatype.DatatypeConstants.DURATION_DAYTIME.getLocalPart().equals("dayTimeDuration"), "DURATION_DAYTIME QName");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
