/*
 * @test
 * @summary javax.print.attribute.standard.JobHoldUntil conformance: white-box structure + black-box behavior.
 * @run main JobHoldUntilTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobHoldUntilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobHoldUntil");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(DateTimeSyntax.class.isAssignableFrom(c), "extends DateTimeSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.util.Date d = new java.util.Date(0);
        JobHoldUntil a = new JobHoldUntil(d);
        check(d.equals(a.getValue()), "getValue");
        check("job-hold-until".equals(a.getName()), "getName");
        check(a.getCategory() == JobHoldUntil.class, "getCategory");
        check(d.toString().equals(a.toString()), "toString == value.toString");
        check(a.equals(new JobHoldUntil(new java.util.Date(0))), "equals same date");
        check(!a.equals(new JobHoldUntil(new java.util.Date(1000))), "different date unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new JobHoldUntil(new java.util.Date(0)).hashCode(), "hashCode consistent");
        boolean threw = false; try { new JobHoldUntil(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null date throws NPE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
