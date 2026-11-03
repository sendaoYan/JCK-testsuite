/*
 * @test
 * @summary javax.print.attribute.standard.JobOriginatingUserName conformance: white-box structure + black-box behavior.
 * @run main JobOriginatingUserNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobOriginatingUserNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobOriginatingUserName");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(TextSyntax.class.isAssignableFrom(c), "extends TextSyntax");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobOriginatingUserName a = new JobOriginatingUserName("report", Locale.ROOT);
        check("report".equals(a.getValue()), "getValue");
        check(Locale.ROOT.equals(a.getLocale()), "getLocale");
        check("job-originating-user-name".equals(a.getName()), "getName");
        check(a.getCategory() == JobOriginatingUserName.class, "getCategory");
        check("report".equals(a.toString()), "toString");
        check(a.equals(new JobOriginatingUserName("report", Locale.ROOT)), "equals same");
        check(!a.equals(new JobOriginatingUserName("report2", Locale.ROOT)), "different value unequal");
        check(!a.equals(new JobOriginatingUserName("report", Locale.US)), "different locale unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new JobOriginatingUserName("report", Locale.ROOT).hashCode(), "hashCode consistent");
        boolean threw = false; try { new JobOriginatingUserName(null, Locale.ROOT); } catch (NullPointerException e) { threw = true; }
        check(threw, "null value throws NPE");
        JobOriginatingUserName def = new JobOriginatingUserName("x", null);
        check(Locale.getDefault().equals(def.getLocale()), "null locale -> default");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
