/*
 * @test
 * @summary javax.xml.datatype.DatatypeFactory conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.datatype.DatatypeFactory f = javax.xml.datatype.DatatypeFactory.newInstance();
        check(f != null, "newInstance");
        javax.xml.datatype.Duration d = f.newDuration("P1Y2M3DT4H5M6S");
        check(d.getYears() == 1 && d.getMonths() == 2 && d.getDays() == 3, "newDuration(String) fields");
        check(d.getHours() == 4 && d.getMinutes() == 5 && d.getSeconds() == 6, "duration time fields");
        check(d.getSign() == 1, "positive duration sign");
        check("P1Y2M3DT4H5M6S".equals(d.toString()), "duration toString round-trip");
        javax.xml.datatype.Duration dm = f.newDuration(-1L);
        check(dm.getSign() == -1, "negative duration sign");
        javax.xml.datatype.Duration dym = f.newDurationYearMonth("P1Y2M");
        check(dym.getYears() == 1 && dym.getMonths() == 2 && dym.getDays() == 0, "year-month duration");
        javax.xml.datatype.Duration ddt = f.newDurationDayTime("P3DT4H");
        check(ddt.getDays() == 3 && ddt.getHours() == 4 && ddt.getMonths() == 0, "day-time duration");
        javax.xml.datatype.XMLGregorianCalendar c = f.newXMLGregorianCalendar("2019-03-04T05:06:07Z");
        check(c.getYear() == 2019 && c.getMonth() == 3 && c.getDay() == 4, "calendar date fields");
        check(c.getHour() == 5 && c.getMinute() == 6 && c.getSecond() == 7 && c.getTimezone() == 0, "calendar time fields");
        check("2019-03-04T05:06:07Z".equals(c.toXMLFormat()), "calendar XML format round-trip");
        javax.xml.datatype.XMLGregorianCalendar c2 = f.newXMLGregorianCalendar(2020, 2, 29, 12, 30, 45, 0, 60);
        check(c2.getYear() == 2020 && c2.getMonth() == 2 && c2.getDay() == 29 && c2.getTimezone() == 60, "int ctor");
        check(c.getXMLSchemaType().equals(javax.xml.datatype.DatatypeConstants.DATETIME), "datetime type");
        check(f.newXMLGregorianCalendarDate(2020, 3, 15, 0).getXMLSchemaType().equals(javax.xml.datatype.DatatypeConstants.DATE), "date type");
        check(f.newXMLGregorianCalendarTime(1, 2, 3, 0).getXMLSchemaType().equals(javax.xml.datatype.DatatypeConstants.TIME), "time type");
        check(javax.xml.datatype.DatatypeFactory.newDefaultInstance() != null, "newDefaultInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
