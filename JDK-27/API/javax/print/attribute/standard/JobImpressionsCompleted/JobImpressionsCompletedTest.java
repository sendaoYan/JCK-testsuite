/*
 * @test
 * @summary javax.print.attribute.standard.JobImpressionsCompleted conformance: white-box structure + black-box behavior.
 * @run main JobImpressionsCompletedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobImpressionsCompletedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobImpressionsCompleted");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(IntegerSyntax.class.isAssignableFrom(c), "extends IntegerSyntax");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobImpressionsCompleted a = new JobImpressionsCompleted(5);
        check(a.getValue() == 5, "getValue");
        check(a.getCategory() == JobImpressionsCompleted.class, "getCategory");
        check("job-impressions-completed".equals(a.getName()), "getName");
        check("5".equals(a.toString()), "toString");
        check(a.equals(new JobImpressionsCompleted(5)), "equals same value");
        check(a.hashCode() == 5, "hashCode == value");
        check(!a.equals(new JobImpressionsCompleted(6)), "different value unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(new JobImpressionsCompleted(0).getValue() == 0, "zero ok");
        boolean threw = false; try { new JobImpressionsCompleted(-1); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "JobImpressionsCompleted(-1) throws IllegalArgumentException");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobImpressionsCompleted(0).getValue() == 0, "min boundary 0 accepted");
        check(new JobImpressionsCompleted(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("JobImpressionsCompleted(-1)", new Runnable() { public void run() { new JobImpressionsCompleted(-1); } }, "Value -1 not in range 0..2147483647");
        check(new JobImpressionsCompleted(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("JobImpressionsCompleted(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobImpressionsCompleted(Integer.MIN_VALUE); } }, null);
        check("job-impressions-completed".equals(new JobImpressionsCompleted(0).getName()), "getName");
        check(JobImpressionsCompleted.class == new JobImpressionsCompleted(0).getCategory(), "getCategory");
        check(new JobImpressionsCompleted(7).equals(new JobImpressionsCompleted(7)), "equals same value");
        check(!new JobImpressionsCompleted(7).equals(new JobImpressionsCompleted(8)), "different value unequal");
        check(!new JobImpressionsCompleted(7).equals(null) && !new JobImpressionsCompleted(7).equals("7"), "null/foreign unequal");
        check(new JobImpressionsCompleted(7).hashCode() == 7, "hashCode == value");
        check(new JobImpressionsCompleted(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobImpressionsCompleted" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobImpressionsCompleted rt = (JobImpressionsCompleted) serRoundTrip(new JobImpressionsCompleted(7));
        check(rt.equals(new JobImpressionsCompleted(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobImpressionsCompleted(12).toString().equals("12"), "toString == value string");
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
