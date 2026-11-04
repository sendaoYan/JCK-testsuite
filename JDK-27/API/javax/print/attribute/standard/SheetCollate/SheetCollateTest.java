/*
 * @test
 * @summary javax.print.attribute.standard.SheetCollate conformance: white-box structure + black-box behavior.
 * @run main SheetCollateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SheetCollateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.SheetCollate");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(SheetCollate.UNCOLLATED.getCategory() == SheetCollate.class, "getCategory");
        check("sheet-collate".equals(SheetCollate.UNCOLLATED.getName()), "getName");
        check(SheetCollate.UNCOLLATED.getValue() == 0, "UNCOLLATED value == 0");
        check("uncollated".equals(SheetCollate.UNCOLLATED.toString()), "UNCOLLATED toString");
        check(SheetCollate.COLLATED.getValue() == 1, "COLLATED value == 1");
        check("collated".equals(SheetCollate.COLLATED.toString()), "COLLATED toString");
        check(SheetCollate.UNCOLLATED.clone() == SheetCollate.UNCOLLATED, "clone returns singleton");
        check(SheetCollate.UNCOLLATED.hashCode() == 0, "hashCode == value");
        check(SheetCollate.UNCOLLATED.equals(SheetCollate.UNCOLLATED), "equals self");
        check(!SheetCollate.UNCOLLATED.equals(SheetCollate.COLLATED), "distinct constants unequal");
        check(!SheetCollate.UNCOLLATED.equals(null) && !SheetCollate.UNCOLLATED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
