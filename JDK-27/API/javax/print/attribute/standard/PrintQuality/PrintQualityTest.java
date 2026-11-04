/*
 * @test
 * @summary javax.print.attribute.standard.PrintQuality conformance: white-box structure + black-box behavior.
 * @run main PrintQualityTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintQualityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrintQuality");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PrintQuality.DRAFT.getCategory() == PrintQuality.class, "getCategory");
        check("print-quality".equals(PrintQuality.DRAFT.getName()), "getName");
        check(PrintQuality.DRAFT.getValue() == 3, "DRAFT value == 3");
        check("draft".equals(PrintQuality.DRAFT.toString()), "DRAFT toString");
        check(PrintQuality.NORMAL.getValue() == 4, "NORMAL value == 4");
        check("normal".equals(PrintQuality.NORMAL.toString()), "NORMAL toString");
        check(PrintQuality.HIGH.getValue() == 5, "HIGH value == 5");
        check("high".equals(PrintQuality.HIGH.toString()), "HIGH toString");
        check(PrintQuality.DRAFT.clone() == PrintQuality.DRAFT, "clone returns singleton");
        check(PrintQuality.DRAFT.hashCode() == 3, "hashCode == value");
        check(PrintQuality.DRAFT.equals(PrintQuality.DRAFT), "equals self");
        check(!PrintQuality.DRAFT.equals(PrintQuality.NORMAL), "distinct constants unequal");
        check(!PrintQuality.DRAFT.equals(null) && !PrintQuality.DRAFT.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
