/*
 * @test
 * @summary javax.xml.datatype.XMLGregorianCalendar conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
