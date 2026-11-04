/*
 * @test
 * @summary javax.print.attribute.standard.DialogTypeSelection conformance: white-box structure + black-box behavior.
 * @run main DialogTypeSelectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DialogTypeSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.DialogTypeSelection");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DialogTypeSelection.NATIVE.getCategory() == DialogTypeSelection.class, "getCategory");
        check("dialog-type-selection".equals(DialogTypeSelection.NATIVE.getName()), "getName");
        check(DialogTypeSelection.NATIVE.getValue() == 0, "NATIVE value == 0");
        check("native".equals(DialogTypeSelection.NATIVE.toString()), "NATIVE toString");
        check(DialogTypeSelection.COMMON.getValue() == 1, "COMMON value == 1");
        check("common".equals(DialogTypeSelection.COMMON.toString()), "COMMON toString");
        check(DialogTypeSelection.NATIVE.clone() == DialogTypeSelection.NATIVE, "clone returns singleton");
        check(DialogTypeSelection.NATIVE.hashCode() == 0, "hashCode == value");
        check(DialogTypeSelection.NATIVE.equals(DialogTypeSelection.NATIVE), "equals self");
        check(!DialogTypeSelection.NATIVE.equals(DialogTypeSelection.COMMON), "distinct constants unequal");
        check(!DialogTypeSelection.NATIVE.equals(null) && !DialogTypeSelection.NATIVE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
