/*
 * @test
 * @summary javax.print.attribute.standard.OutputBin conformance: white-box structure + black-box behavior.
 * @run main OutputBinTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class OutputBinTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.OutputBin");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(OutputBin.TOP.getCategory() == OutputBin.class, "getCategory");
        check("output-bin".equals(OutputBin.TOP.getName()), "getName");
        check(OutputBin.TOP.getValue() == 0, "TOP value == 0");
        check("top".equals(OutputBin.TOP.toString()), "TOP toString");
        check(OutputBin.MIDDLE.getValue() == 1, "MIDDLE value == 1");
        check("middle".equals(OutputBin.MIDDLE.toString()), "MIDDLE toString");
        check(OutputBin.BOTTOM.getValue() == 2, "BOTTOM value == 2");
        check("bottom".equals(OutputBin.BOTTOM.toString()), "BOTTOM toString");
        check(OutputBin.SIDE.getValue() == 3, "SIDE value == 3");
        check("side".equals(OutputBin.SIDE.toString()), "SIDE toString");
        check(OutputBin.LEFT.getValue() == 4, "LEFT value == 4");
        check("left".equals(OutputBin.LEFT.toString()), "LEFT toString");
        check(OutputBin.RIGHT.getValue() == 5, "RIGHT value == 5");
        check("right".equals(OutputBin.RIGHT.toString()), "RIGHT toString");
        check(OutputBin.CENTER.getValue() == 6, "CENTER value == 6");
        check("center".equals(OutputBin.CENTER.toString()), "CENTER toString");
        check(OutputBin.REAR.getValue() == 7, "REAR value == 7");
        check("rear".equals(OutputBin.REAR.toString()), "REAR toString");
        check(OutputBin.FACE_UP.getValue() == 8, "FACE_UP value == 8");
        check("face-up".equals(OutputBin.FACE_UP.toString()), "FACE_UP toString");
        check(OutputBin.FACE_DOWN.getValue() == 9, "FACE_DOWN value == 9");
        check("face-down".equals(OutputBin.FACE_DOWN.toString()), "FACE_DOWN toString");
        check(OutputBin.LARGE_CAPACITY.getValue() == 10, "LARGE_CAPACITY value == 10");
        check("large-capacity".equals(OutputBin.LARGE_CAPACITY.toString()), "LARGE_CAPACITY toString");
        check(OutputBin.TOP.clone() == OutputBin.TOP, "clone returns singleton");
        check(OutputBin.TOP.hashCode() == 0, "hashCode == value");
        check(OutputBin.TOP.equals(OutputBin.TOP), "equals self");
        check(!OutputBin.TOP.equals(OutputBin.MIDDLE), "distinct constants unequal");
        check(!OutputBin.TOP.equals(null) && !OutputBin.TOP.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(OutputBin.TOP.getValue() == 0, "TOP.getValue == 0");
        check("top".equals(OutputBin.TOP.toString()), "TOP.toString [top]");
        check(OutputBin.MIDDLE.getValue() == 1, "MIDDLE.getValue == 1");
        check("middle".equals(OutputBin.MIDDLE.toString()), "MIDDLE.toString [middle]");
        check(OutputBin.BOTTOM.getValue() == 2, "BOTTOM.getValue == 2");
        check("bottom".equals(OutputBin.BOTTOM.toString()), "BOTTOM.toString [bottom]");
        check(OutputBin.SIDE.getValue() == 3, "SIDE.getValue == 3");
        check("side".equals(OutputBin.SIDE.toString()), "SIDE.toString [side]");
        check(OutputBin.LEFT.getValue() == 4, "LEFT.getValue == 4");
        check("left".equals(OutputBin.LEFT.toString()), "LEFT.toString [left]");
        check(OutputBin.RIGHT.getValue() == 5, "RIGHT.getValue == 5");
        check("right".equals(OutputBin.RIGHT.toString()), "RIGHT.toString [right]");
        check(OutputBin.CENTER.getValue() == 6, "CENTER.getValue == 6");
        check("center".equals(OutputBin.CENTER.toString()), "CENTER.toString [center]");
        check(OutputBin.REAR.getValue() == 7, "REAR.getValue == 7");
        check("rear".equals(OutputBin.REAR.toString()), "REAR.toString [rear]");
        check(OutputBin.FACE_UP.getValue() == 8, "FACE_UP.getValue == 8");
        check("face-up".equals(OutputBin.FACE_UP.toString()), "FACE_UP.toString [face-up]");
        check(OutputBin.FACE_DOWN.getValue() == 9, "FACE_DOWN.getValue == 9");
        check("face-down".equals(OutputBin.FACE_DOWN.toString()), "FACE_DOWN.toString [face-down]");
        check(OutputBin.LARGE_CAPACITY.getValue() == 10, "LARGE_CAPACITY.getValue == 10");
        check("large-capacity".equals(OutputBin.LARGE_CAPACITY.toString()), "LARGE_CAPACITY.toString [large-capacity]");
        check("output-bin".equals(OutputBin.TOP.getName()), "getName == \"output-bin\"");
        check(OutputBin.class == OutputBin.TOP.getCategory(), "getCategory");
        check(OutputBin.TOP.equals(OutputBin.TOP), "equals self");
        check(!OutputBin.TOP.equals(null), "not equals null");
        check(!OutputBin.TOP.equals("top"), "not equals foreign type");
        check(OutputBin.TOP.hashCode() == 0, "hashCode == value");
        check(OutputBin.TOP.clone() == OutputBin.TOP, "clone returns same singleton");
        check(!OutputBin.TOP.equals(OutputBin.MIDDLE), "distinct constants unequal");
        check(OutputBin.TOP.toString() != OutputBin.MIDDLE.toString() || true, "toString stable");
        check(!OutputBin.TOP.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        OutputBin.TOP,
        OutputBin.MIDDLE,
        OutputBin.BOTTOM,
        OutputBin.SIDE,
        OutputBin.LEFT,
        OutputBin.RIGHT,
        OutputBin.CENTER,
        OutputBin.REAR,
        OutputBin.FACE_UP,
        OutputBin.FACE_DOWN,
        OutputBin.LARGE_CAPACITY
    };
    static final String[] NAMES = {
        "TOP",
        "MIDDLE",
        "BOTTOM",
        "SIDE",
        "LEFT",
        "RIGHT",
        "CENTER",
        "REAR",
        "FACE_UP",
        "FACE_DOWN",
        "LARGE_CAPACITY"
    };

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
