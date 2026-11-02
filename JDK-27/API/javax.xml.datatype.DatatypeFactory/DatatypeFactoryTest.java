/*
 * @test
 * @summary javax.xml.datatype.DatatypeFactory conformance: API type verification.
 * @run main DatatypeFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DatatypeFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.datatype.DatatypeFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newDuration"), "method newDuration");
        check(hasMethod(c, "newDurationDayTime"), "method newDurationDayTime");
        check(hasMethod(c, "newDurationYearMonth"), "method newDurationYearMonth");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newXMLGregorianCalendar"), "method newXMLGregorianCalendar");
        check(hasMethod(c, "newXMLGregorianCalendarDate"), "method newXMLGregorianCalendarDate");
        check(hasMethod(c, "newXMLGregorianCalendarTime"), "method newXMLGregorianCalendarTime");
        check(hasField(c, "DATATYPEFACTORY_IMPLEMENTATION_CLASS"), "field DATATYPEFACTORY_IMPLEMENTATION_CLASS");
        check(hasField(c, "DATATYPEFACTORY_PROPERTY"), "field DATATYPEFACTORY_PROPERTY");

        javax.xml.datatype.DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.Duration d = df.newDuration("P1Y2M3DT4H5M6S");
        check(d.getYears() == 1, "duration years");
        javax.xml.datatype.XMLGregorianCalendar x = df.newXMLGregorianCalendar("2001-02-03T04:05:06Z");
        check(x.getYear() == 2001, "year");
        check(df.newDurationYearMonth("P1Y").getYears() == 1, "yearMonth");
        check(df.newDurationDayTime("P1DT1H").getDays() == 1, "dayTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
