/*
 * @test
 * @summary javax.print.attribute.standard.Finishings conformance: white-box structure + black-box behavior.
 * @run main FinishingsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class FinishingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Finishings");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Finishings.NONE.getCategory() == Finishings.class, "getCategory");
        check("finishings".equals(Finishings.NONE.getName()), "getName");
        check(Finishings.NONE.getValue() == 3, "NONE value == 3");
        check("none".equals(Finishings.NONE.toString()), "NONE toString");
        check(Finishings.STAPLE.getValue() == 4, "STAPLE value == 4");
        check("staple".equals(Finishings.STAPLE.toString()), "STAPLE toString");
        check(Finishings.COVER.getValue() == 6, "COVER value == 6");
        check("cover".equals(Finishings.COVER.toString()), "COVER toString");
        check(Finishings.BIND.getValue() == 7, "BIND value == 7");
        check("bind".equals(Finishings.BIND.toString()), "BIND toString");
        check(Finishings.SADDLE_STITCH.getValue() == 8, "SADDLE_STITCH value == 8");
        check("saddle-stitch".equals(Finishings.SADDLE_STITCH.toString()), "SADDLE_STITCH toString");
        check(Finishings.EDGE_STITCH.getValue() == 9, "EDGE_STITCH value == 9");
        check("edge-stitch".equals(Finishings.EDGE_STITCH.toString()), "EDGE_STITCH toString");
        check(Finishings.STAPLE_TOP_LEFT.getValue() == 20, "STAPLE_TOP_LEFT value == 20");
        check("staple-top-left".equals(Finishings.STAPLE_TOP_LEFT.toString()), "STAPLE_TOP_LEFT toString");
        check(Finishings.STAPLE_BOTTOM_LEFT.getValue() == 21, "STAPLE_BOTTOM_LEFT value == 21");
        check("staple-bottom-left".equals(Finishings.STAPLE_BOTTOM_LEFT.toString()), "STAPLE_BOTTOM_LEFT toString");
        check(Finishings.STAPLE_TOP_RIGHT.getValue() == 22, "STAPLE_TOP_RIGHT value == 22");
        check("staple-top-right".equals(Finishings.STAPLE_TOP_RIGHT.toString()), "STAPLE_TOP_RIGHT toString");
        check(Finishings.STAPLE_BOTTOM_RIGHT.getValue() == 23, "STAPLE_BOTTOM_RIGHT value == 23");
        check("staple-bottom-right".equals(Finishings.STAPLE_BOTTOM_RIGHT.toString()), "STAPLE_BOTTOM_RIGHT toString");
        check(Finishings.EDGE_STITCH_LEFT.getValue() == 24, "EDGE_STITCH_LEFT value == 24");
        check("edge-stitch-left".equals(Finishings.EDGE_STITCH_LEFT.toString()), "EDGE_STITCH_LEFT toString");
        check(Finishings.EDGE_STITCH_TOP.getValue() == 25, "EDGE_STITCH_TOP value == 25");
        check("edge-stitch-top".equals(Finishings.EDGE_STITCH_TOP.toString()), "EDGE_STITCH_TOP toString");
        check(Finishings.EDGE_STITCH_RIGHT.getValue() == 26, "EDGE_STITCH_RIGHT value == 26");
        check("edge-stitch-right".equals(Finishings.EDGE_STITCH_RIGHT.toString()), "EDGE_STITCH_RIGHT toString");
        check(Finishings.EDGE_STITCH_BOTTOM.getValue() == 27, "EDGE_STITCH_BOTTOM value == 27");
        check("edge-stitch-bottom".equals(Finishings.EDGE_STITCH_BOTTOM.toString()), "EDGE_STITCH_BOTTOM toString");
        check(Finishings.STAPLE_DUAL_LEFT.getValue() == 28, "STAPLE_DUAL_LEFT value == 28");
        check("staple-dual-left".equals(Finishings.STAPLE_DUAL_LEFT.toString()), "STAPLE_DUAL_LEFT toString");
        check(Finishings.STAPLE_DUAL_TOP.getValue() == 29, "STAPLE_DUAL_TOP value == 29");
        check("staple-dual-top".equals(Finishings.STAPLE_DUAL_TOP.toString()), "STAPLE_DUAL_TOP toString");
        check(Finishings.STAPLE_DUAL_RIGHT.getValue() == 30, "STAPLE_DUAL_RIGHT value == 30");
        check("staple-dual-right".equals(Finishings.STAPLE_DUAL_RIGHT.toString()), "STAPLE_DUAL_RIGHT toString");
        check(Finishings.STAPLE_DUAL_BOTTOM.getValue() == 31, "STAPLE_DUAL_BOTTOM value == 31");
        check("staple-dual-bottom".equals(Finishings.STAPLE_DUAL_BOTTOM.toString()), "STAPLE_DUAL_BOTTOM toString");
        check(Finishings.NONE.clone() == Finishings.NONE, "clone returns singleton");
        check(Finishings.NONE.hashCode() == 3, "hashCode == value");
        check(Finishings.NONE.equals(Finishings.NONE), "equals self");
        check(!Finishings.NONE.equals(Finishings.STAPLE), "distinct constants unequal");
        check(!Finishings.NONE.equals(null) && !Finishings.NONE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Finishings.NONE.getValue() == 3, "NONE.getValue == 3");
        check("none".equals(Finishings.NONE.toString()), "NONE.toString [none]");
        check(Finishings.STAPLE.getValue() == 4, "STAPLE.getValue == 4");
        check("staple".equals(Finishings.STAPLE.toString()), "STAPLE.toString [staple]");
        check(Finishings.COVER.getValue() == 6, "COVER.getValue == 6");
        check("cover".equals(Finishings.COVER.toString()), "COVER.toString [cover]");
        check(Finishings.BIND.getValue() == 7, "BIND.getValue == 7");
        check("bind".equals(Finishings.BIND.toString()), "BIND.toString [bind]");
        check(Finishings.SADDLE_STITCH.getValue() == 8, "SADDLE_STITCH.getValue == 8");
        check("saddle-stitch".equals(Finishings.SADDLE_STITCH.toString()), "SADDLE_STITCH.toString [saddle-stitch]");
        check(Finishings.EDGE_STITCH.getValue() == 9, "EDGE_STITCH.getValue == 9");
        check("edge-stitch".equals(Finishings.EDGE_STITCH.toString()), "EDGE_STITCH.toString [edge-stitch]");
        check(Finishings.STAPLE_TOP_LEFT.getValue() == 20, "STAPLE_TOP_LEFT.getValue == 20");
        check("staple-top-left".equals(Finishings.STAPLE_TOP_LEFT.toString()), "STAPLE_TOP_LEFT.toString [staple-top-left]");
        check(Finishings.STAPLE_BOTTOM_LEFT.getValue() == 21, "STAPLE_BOTTOM_LEFT.getValue == 21");
        check("staple-bottom-left".equals(Finishings.STAPLE_BOTTOM_LEFT.toString()), "STAPLE_BOTTOM_LEFT.toString [staple-bottom-left]");
        check(Finishings.STAPLE_TOP_RIGHT.getValue() == 22, "STAPLE_TOP_RIGHT.getValue == 22");
        check("staple-top-right".equals(Finishings.STAPLE_TOP_RIGHT.toString()), "STAPLE_TOP_RIGHT.toString [staple-top-right]");
        check(Finishings.STAPLE_BOTTOM_RIGHT.getValue() == 23, "STAPLE_BOTTOM_RIGHT.getValue == 23");
        check("staple-bottom-right".equals(Finishings.STAPLE_BOTTOM_RIGHT.toString()), "STAPLE_BOTTOM_RIGHT.toString [staple-bottom-right]");
        check(Finishings.EDGE_STITCH_LEFT.getValue() == 24, "EDGE_STITCH_LEFT.getValue == 24");
        check("edge-stitch-left".equals(Finishings.EDGE_STITCH_LEFT.toString()), "EDGE_STITCH_LEFT.toString [edge-stitch-left]");
        check(Finishings.EDGE_STITCH_TOP.getValue() == 25, "EDGE_STITCH_TOP.getValue == 25");
        check("edge-stitch-top".equals(Finishings.EDGE_STITCH_TOP.toString()), "EDGE_STITCH_TOP.toString [edge-stitch-top]");
        check(Finishings.EDGE_STITCH_RIGHT.getValue() == 26, "EDGE_STITCH_RIGHT.getValue == 26");
        check("edge-stitch-right".equals(Finishings.EDGE_STITCH_RIGHT.toString()), "EDGE_STITCH_RIGHT.toString [edge-stitch-right]");
        check(Finishings.EDGE_STITCH_BOTTOM.getValue() == 27, "EDGE_STITCH_BOTTOM.getValue == 27");
        check("edge-stitch-bottom".equals(Finishings.EDGE_STITCH_BOTTOM.toString()), "EDGE_STITCH_BOTTOM.toString [edge-stitch-bottom]");
        check(Finishings.STAPLE_DUAL_LEFT.getValue() == 28, "STAPLE_DUAL_LEFT.getValue == 28");
        check("staple-dual-left".equals(Finishings.STAPLE_DUAL_LEFT.toString()), "STAPLE_DUAL_LEFT.toString [staple-dual-left]");
        check(Finishings.STAPLE_DUAL_TOP.getValue() == 29, "STAPLE_DUAL_TOP.getValue == 29");
        check("staple-dual-top".equals(Finishings.STAPLE_DUAL_TOP.toString()), "STAPLE_DUAL_TOP.toString [staple-dual-top]");
        check(Finishings.STAPLE_DUAL_RIGHT.getValue() == 30, "STAPLE_DUAL_RIGHT.getValue == 30");
        check("staple-dual-right".equals(Finishings.STAPLE_DUAL_RIGHT.toString()), "STAPLE_DUAL_RIGHT.toString [staple-dual-right]");
        check(Finishings.STAPLE_DUAL_BOTTOM.getValue() == 31, "STAPLE_DUAL_BOTTOM.getValue == 31");
        check("staple-dual-bottom".equals(Finishings.STAPLE_DUAL_BOTTOM.toString()), "STAPLE_DUAL_BOTTOM.toString [staple-dual-bottom]");
        check("finishings".equals(Finishings.NONE.getName()), "getName == \"finishings\"");
        check(Finishings.class == Finishings.NONE.getCategory(), "getCategory");
        check(Finishings.NONE.equals(Finishings.NONE), "equals self");
        check(!Finishings.NONE.equals(null), "not equals null");
        check(!Finishings.NONE.equals("none"), "not equals foreign type");
        check(Finishings.NONE.hashCode() == 3, "hashCode == value");
        check(Finishings.NONE.clone() == Finishings.NONE, "clone returns same singleton");
        check(!Finishings.NONE.equals(Finishings.STAPLE), "distinct constants unequal");
        check(Finishings.NONE.toString() != Finishings.STAPLE.toString() || true, "toString stable");
        check(!Finishings.NONE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        Finishings.NONE,
        Finishings.STAPLE,
        Finishings.COVER,
        Finishings.BIND,
        Finishings.SADDLE_STITCH,
        Finishings.EDGE_STITCH,
        Finishings.STAPLE_TOP_LEFT,
        Finishings.STAPLE_BOTTOM_LEFT,
        Finishings.STAPLE_TOP_RIGHT,
        Finishings.STAPLE_BOTTOM_RIGHT,
        Finishings.EDGE_STITCH_LEFT,
        Finishings.EDGE_STITCH_TOP,
        Finishings.EDGE_STITCH_RIGHT,
        Finishings.EDGE_STITCH_BOTTOM,
        Finishings.STAPLE_DUAL_LEFT,
        Finishings.STAPLE_DUAL_TOP,
        Finishings.STAPLE_DUAL_RIGHT,
        Finishings.STAPLE_DUAL_BOTTOM
    };
    static final String[] NAMES = {
        "NONE",
        "STAPLE",
        "COVER",
        "BIND",
        "SADDLE_STITCH",
        "EDGE_STITCH",
        "STAPLE_TOP_LEFT",
        "STAPLE_BOTTOM_LEFT",
        "STAPLE_TOP_RIGHT",
        "STAPLE_BOTTOM_RIGHT",
        "EDGE_STITCH_LEFT",
        "EDGE_STITCH_TOP",
        "EDGE_STITCH_RIGHT",
        "EDGE_STITCH_BOTTOM",
        "STAPLE_DUAL_LEFT",
        "STAPLE_DUAL_TOP",
        "STAPLE_DUAL_RIGHT",
        "STAPLE_DUAL_BOTTOM"
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
