/*
 * @test
 * @summary javax.print.attribute.standard.DateTimeAtProcessing conformance: white-box structure + black-box behavior.
 * @run main DateTimeAtProcessingTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DateTimeAtProcessingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.DateTimeAtProcessing");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(DateTimeSyntax.class.isAssignableFrom(c), "extends DateTimeSyntax");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.util.Date d = new java.util.Date(0);
        DateTimeAtProcessing a = new DateTimeAtProcessing(d);
        check(d.equals(a.getValue()), "getValue");
        check("date-time-at-processing".equals(a.getName()), "getName");
        check(a.getCategory() == DateTimeAtProcessing.class, "getCategory");
        check(d.toString().equals(a.toString()), "toString == value.toString");
        check(a.equals(new DateTimeAtProcessing(new java.util.Date(0))), "equals same date");
        check(!a.equals(new DateTimeAtProcessing(new java.util.Date(1000))), "different date unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new DateTimeAtProcessing(new java.util.Date(0)).hashCode(), "hashCode consistent");
        boolean threw = false; try { new DateTimeAtProcessing(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null date throws NPE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: millis preservation / defensive copy / null NPE / serialization ----
    static void testContractDeep() throws Exception {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar(2026, java.util.Calendar.NOVEMBER, 5, 12, 34, 56);
        cal.set(java.util.Calendar.MILLISECOND, 789);
        long epoch = cal.getTimeInMillis();
        DateTimeAtProcessing v = new DateTimeAtProcessing(cal.getTime());
        check(v.getValue().getTime() == epoch, "millis preserved exactly");
        java.util.Date got = v.getValue();
        check(got != cal.getTime(), "getValue returns new Date");
        got.setTime(0L);
        check(v.getValue().getTime() == epoch, "getValue defensive copy independent");
        check("date-time-at-processing".equals(v.getName()), "getName");
        check(DateTimeAtProcessing.class == v.getCategory(), "getCategory");
        expectNPE("DateTimeAtProcessing(null)", new Runnable() { public void run() { new DateTimeAtProcessing(null); } }, "value is null");
        check(v.equals(new DateTimeAtProcessing(new java.util.Date(epoch))), "equals same instant");
        check(!v.equals(new DateTimeAtProcessing(new java.util.Date(epoch + 1))), "different instant unequal");
        check(!v.equals(null) && !v.equals(new java.util.Date(epoch)), "null/foreign unequal");
        check(v.hashCode() == new DateTimeAtProcessing(new java.util.Date(epoch)).hashCode(), "hashCode consistent");
        DateTimeAtProcessing rt = (DateTimeAtProcessing) serRoundTrip(v);
        check(rt.equals(v) && rt.getValue().getTime() == epoch, "serialization round-trip");
        check(v.toString() != null && v.toString().length() > 0, "toString non-null");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
