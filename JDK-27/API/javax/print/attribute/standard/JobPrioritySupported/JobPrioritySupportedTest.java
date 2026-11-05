/*
 * @test
 * @summary javax.print.attribute.standard.JobPrioritySupported conformance: white-box structure + black-box behavior.
 * @run main JobPrioritySupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobPrioritySupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobPrioritySupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(IntegerSyntax.class.isAssignableFrom(c), "extends IntegerSyntax");
        check(SupportedValuesAttribute.class.isAssignableFrom(c), "implements SupportedValuesAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobPrioritySupported a = new JobPrioritySupported(5);
        check(a.getValue() == 5, "getValue");
        check(a.getCategory() == JobPrioritySupported.class, "getCategory");
        check("job-priority-supported".equals(a.getName()), "getName");
        check("5".equals(a.toString()), "toString");
        check(a.equals(new JobPrioritySupported(5)), "equals same value");
        check(a.hashCode() == 5, "hashCode == value");
        check(!a.equals(new JobPrioritySupported(6)), "different value unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(new JobPrioritySupported(1).getValue() == 1, "lower bound ok");
        boolean threw = false; try { new JobPrioritySupported(0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "JobPrioritySupported(0) throws IllegalArgumentException");
        boolean threwU = false; try { new JobPrioritySupported(101); } catch (IllegalArgumentException e) { threwU = true; }
        check(threwU, "JobPrioritySupported(101) throws IllegalArgumentException");
        check(new JobPrioritySupported(100).getValue() == 100, "upper bound ok");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new JobPrioritySupported(1).getValue() == 1, "min boundary 1 accepted");
        check(new JobPrioritySupported(100).getValue() == 100, "max boundary 100 accepted");
        expectIAE("JobPrioritySupported(0)", new Runnable() { public void run() { new JobPrioritySupported(0); } }, "Value 0 not in range 1..100");
        expectIAE("JobPrioritySupported(101)", new Runnable() { public void run() { new JobPrioritySupported(101); } }, "Value 101 not in range 1..100");
        expectIAE("JobPrioritySupported(Integer.MIN_VALUE)", new Runnable() { public void run() { new JobPrioritySupported(Integer.MIN_VALUE); } }, null);
        check("job-priority-supported".equals(new JobPrioritySupported(1).getName()), "getName");
        check(JobPrioritySupported.class == new JobPrioritySupported(1).getCategory(), "getCategory");
        check(new JobPrioritySupported(7).equals(new JobPrioritySupported(7)), "equals same value");
        check(!new JobPrioritySupported(7).equals(new JobPrioritySupported(8)), "different value unequal");
        check(!new JobPrioritySupported(7).equals(null) && !new JobPrioritySupported(7).equals("7"), "null/foreign unequal");
        check(new JobPrioritySupported(7).hashCode() == 7, "hashCode == value");
        check(new JobPrioritySupported(7).equals(new javax.print.attribute.standard.Copies(7)) == ("JobPrioritySupported" == "Copies"), "cross IntegerSyntax subclass equals false");
        JobPrioritySupported rt = (JobPrioritySupported) serRoundTrip(new JobPrioritySupported(7));
        check(rt.equals(new JobPrioritySupported(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new JobPrioritySupported(12).toString().equals("12"), "toString == value string");
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
