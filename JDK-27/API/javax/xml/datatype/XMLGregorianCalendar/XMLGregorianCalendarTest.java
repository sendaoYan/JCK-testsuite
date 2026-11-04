/*
 * @test
 * @summary javax.xml.datatype.XMLGregorianCalendar conformance: white-box structure + black-box behavior.
 * @run main XMLGregorianCalendarTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLGregorianCalendarTest {
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

        Class<?> c = Class.forName("javax.xml.datatype.XMLGregorianCalendar");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "compare"), "method compare");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getDay"), "method getDay");
        check(hasMethod(c, "getEon"), "method getEon");
        check(hasMethod(c, "getEonAndYear"), "method getEonAndYear");
        check(hasMethod(c, "getFractionalSecond"), "method getFractionalSecond");
        check(hasMethod(c, "getHour"), "method getHour");
        check(hasMethod(c, "getMillisecond"), "method getMillisecond");
        check(hasMethod(c, "getMinute"), "method getMinute");
        check(hasMethod(c, "getMonth"), "method getMonth");
        check(hasMethod(c, "getSecond"), "method getSecond");
        check(hasMethod(c, "getTimeZone"), "method getTimeZone");
        check(hasMethod(c, "getTimezone"), "method getTimezone");
        check(hasMethod(c, "getXMLSchemaType"), "method getXMLSchemaType");
        check(hasMethod(c, "getYear"), "method getYear");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setDay"), "method setDay");
        check(hasMethod(c, "setFractionalSecond"), "method setFractionalSecond");
        check(hasMethod(c, "setHour"), "method setHour");
        check(hasMethod(c, "setMillisecond"), "method setMillisecond");
        check(hasMethod(c, "setMinute"), "method setMinute");
        check(hasMethod(c, "setMonth"), "method setMonth");
        check(hasMethod(c, "setSecond"), "method setSecond");
        check(hasMethod(c, "setTime"), "method setTime");
        check(hasMethod(c, "setTimezone"), "method setTimezone");
        check(hasMethod(c, "setYear"), "method setYear");
        check(hasMethod(c, "toGregorianCalendar"), "method toGregorianCalendar");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "toXMLFormat"), "method toXMLFormat");

        javax.xml.datatype.DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.XMLGregorianCalendar x = df.newXMLGregorianCalendar("2001-02-03T04:05:06.789Z");
        check(x.getYear() == 2001, "year");
        check(x.getMonth() == 2, "month");
        check(x.getDay() == 3, "day");
        check(x.getHour() == 4, "hour");
        check(x.getMinute() == 5, "minute");
        check(x.getSecond() == 6, "second");
        check(x.getXMLSchemaType() == javax.xml.datatype.DatatypeConstants.DATETIME, "schemaType");
        check(x.toXMLFormat() != null, "toXMLFormat");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.datatype.DatatypeFactory f = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.XMLGregorianCalendar c = f.newXMLGregorianCalendar("2019-03-04T05:06:07.5Z");
        check(c.getYear() == 2019 && c.getMonth() == 3 && c.getDay() == 4, "date fields");
        check(c.getHour() == 5 && c.getMinute() == 6 && c.getSecond() == 7, "time fields");
        check(c.getFractionalSecond() != null && c.getFractionalSecond().compareTo(new java.math.BigDecimal("0.5")) == 0, "fractional second");
        check(c.getTimezone() == 0, "UTC timezone");
        check(c.isValid(), "valid");
        check("2019-03-04T05:06:07.5Z".equals(c.toXMLFormat()), "XML format round-trip");
        javax.xml.datatype.XMLGregorianCalendar c2 = f.newXMLGregorianCalendar("2020-12-31T23:59:59+01:00");
        check(c.compare(c2) < 0, "compare chronological");
        javax.xml.datatype.XMLGregorianCalendar c3 = (javax.xml.datatype.XMLGregorianCalendar) c.clone();
        check(c3 != c && c3.equals(c) && c3.hashCode() == c.hashCode(), "clone");
        check(c.getXMLSchemaType().equals(javax.xml.datatype.DatatypeConstants.DATETIME), "schema type");
        java.util.GregorianCalendar gc = c.toGregorianCalendar();
        check(gc.get(java.util.Calendar.YEAR) == 2019, "toGregorianCalendar year");
        check(c.equals(f.newXMLGregorianCalendar("2019-03-04T05:06:07.5Z")), "equals");
        c.setDay(15); check(c.getDay() == 15, "setDay");
        c.reset(); check(c.getDay() == 4, "reset restores original day");
        javax.xml.datatype.XMLGregorianCalendar c4 = f.newXMLGregorianCalendar("2019-03-04T05:06:07.5");
        javax.xml.datatype.XMLGregorianCalendar n = c4.normalize();
        check(n.getTimezone() == javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED, "normalize keeps undefined timezone");
        javax.xml.datatype.XMLGregorianCalendar c5 = f.newXMLGregorianCalendar("2000-03-04T23:00:00+03:00");
        check(c5.normalize().getTimezone() == 0, "normalize converts zoned calendar to UTC");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
