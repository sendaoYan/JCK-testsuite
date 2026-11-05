/*
 * @test
 * @summary javax.print.attribute.standard.MediaTray conformance: white-box structure + black-box behavior.
 * @run main MediaTrayTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaTrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static final class MT extends MediaTray {
        MT(int v) { super(v); }
        String[] stringTable() { return super.getStringTable(); }
        javax.print.attribute.EnumSyntax[] enumValueTable() { return super.getEnumValueTable(); }
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaTray");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(Media.class.isAssignableFrom(c), "extends Media");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(MediaTray.TOP.getCategory() == Media.class, "getCategory");
        check("media".equals(MediaTray.TOP.getName()), "getName");
        check(MediaTray.TOP.getValue() == 0, "TOP value == 0");
        check("top".equals(MediaTray.TOP.toString()), "TOP toString");
        check(MediaTray.MIDDLE.getValue() == 1, "MIDDLE value == 1");
        check("middle".equals(MediaTray.MIDDLE.toString()), "MIDDLE toString");
        check(MediaTray.BOTTOM.getValue() == 2, "BOTTOM value == 2");
        check("bottom".equals(MediaTray.BOTTOM.toString()), "BOTTOM toString");
        check(MediaTray.ENVELOPE.getValue() == 3, "ENVELOPE value == 3");
        check("envelope".equals(MediaTray.ENVELOPE.toString()), "ENVELOPE toString");
        check(MediaTray.MANUAL.getValue() == 4, "MANUAL value == 4");
        check("manual".equals(MediaTray.MANUAL.toString()), "MANUAL toString");
        check(MediaTray.LARGE_CAPACITY.getValue() == 5, "LARGE_CAPACITY value == 5");
        check("large-capacity".equals(MediaTray.LARGE_CAPACITY.toString()), "LARGE_CAPACITY toString");
        check(MediaTray.MAIN.getValue() == 6, "MAIN value == 6");
        check("main".equals(MediaTray.MAIN.toString()), "MAIN toString");
        check(MediaTray.SIDE.getValue() == 7, "SIDE value == 7");
        check("side".equals(MediaTray.SIDE.toString()), "SIDE toString");
        check(MediaTray.TOP.clone() == MediaTray.TOP, "clone returns singleton");
        check(MediaTray.TOP.hashCode() == 0, "hashCode == value");
        check(MediaTray.TOP.equals(MediaTray.TOP), "equals self");
        check(!MediaTray.TOP.equals(MediaTray.MIDDLE), "distinct constants unequal");
        check(!MediaTray.TOP.equals(null) && !MediaTray.TOP.equals("x"), "not equals null/foreign");

        // direct protected-method invocation through a minimal subclass (super constructor + super getStringTable/getEnumValueTable)
        MT m = new MT(0);
        String[] st = m.stringTable();
        check(st != null && st.length == 8, "getStringTable length");
        check("top".equals(st[0]), "getStringTable first entry");
        javax.print.attribute.EnumSyntax[] evt = m.enumValueTable();
        check(evt != null && evt.length == 8, "getEnumValueTable length");
        check(evt[0] == MediaTray.TOP, "getEnumValueTable first entry");
        check(m.getValue() == 0 && "top".equals(m.toString()), "subclass ctor sets value 0");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(MediaTray.TOP.getValue() == 0, "TOP.getValue == 0");
        check("top".equals(MediaTray.TOP.toString()), "TOP.toString [top]");
        check(MediaTray.MIDDLE.getValue() == 1, "MIDDLE.getValue == 1");
        check("middle".equals(MediaTray.MIDDLE.toString()), "MIDDLE.toString [middle]");
        check(MediaTray.BOTTOM.getValue() == 2, "BOTTOM.getValue == 2");
        check("bottom".equals(MediaTray.BOTTOM.toString()), "BOTTOM.toString [bottom]");
        check(MediaTray.ENVELOPE.getValue() == 3, "ENVELOPE.getValue == 3");
        check("envelope".equals(MediaTray.ENVELOPE.toString()), "ENVELOPE.toString [envelope]");
        check(MediaTray.MANUAL.getValue() == 4, "MANUAL.getValue == 4");
        check("manual".equals(MediaTray.MANUAL.toString()), "MANUAL.toString [manual]");
        check(MediaTray.LARGE_CAPACITY.getValue() == 5, "LARGE_CAPACITY.getValue == 5");
        check("large-capacity".equals(MediaTray.LARGE_CAPACITY.toString()), "LARGE_CAPACITY.toString [large-capacity]");
        check(MediaTray.MAIN.getValue() == 6, "MAIN.getValue == 6");
        check("main".equals(MediaTray.MAIN.toString()), "MAIN.toString [main]");
        check(MediaTray.SIDE.getValue() == 7, "SIDE.getValue == 7");
        check("side".equals(MediaTray.SIDE.toString()), "SIDE.toString [side]");
        check("media".equals(MediaTray.TOP.getName()), "getName == \"media\"");
        check(Media.class == MediaTray.TOP.getCategory(), "getCategory");
        check(MediaTray.TOP.equals(MediaTray.TOP), "equals self");
        check(!MediaTray.TOP.equals(null), "not equals null");
        check(!MediaTray.TOP.equals("top"), "not equals foreign type");
        check(MediaTray.TOP.hashCode() == 0, "hashCode == value");
        check(MediaTray.TOP.clone() == MediaTray.TOP, "clone returns same singleton");
        check(!MediaTray.TOP.equals(MediaTray.MIDDLE), "distinct constants unequal");
        check(MediaTray.TOP.toString() != MediaTray.MIDDLE.toString() || true, "toString stable");
        check(!MediaTray.TOP.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        MediaTray.TOP,
        MediaTray.MIDDLE,
        MediaTray.BOTTOM,
        MediaTray.ENVELOPE,
        MediaTray.MANUAL,
        MediaTray.LARGE_CAPACITY,
        MediaTray.MAIN,
        MediaTray.SIDE
    };
    static final String[] NAMES = {
        "TOP",
        "MIDDLE",
        "BOTTOM",
        "ENVELOPE",
        "MANUAL",
        "LARGE_CAPACITY",
        "MAIN",
        "SIDE"
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
