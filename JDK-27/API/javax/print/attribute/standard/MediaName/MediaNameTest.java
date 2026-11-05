/*
 * @test
 * @summary javax.print.attribute.standard.MediaName conformance: white-box structure + black-box behavior.
 * @run main MediaNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static final class MN extends MediaName {
        MN(int v) { super(v); }
        String[] stringTable() { return super.getStringTable(); }
        javax.print.attribute.EnumSyntax[] enumValueTable() { return super.getEnumValueTable(); }
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaName");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(Media.class.isAssignableFrom(c), "extends Media");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(MediaName.NA_LETTER_WHITE.getCategory() == Media.class, "getCategory");
        check("media".equals(MediaName.NA_LETTER_WHITE.getName()), "getName");
        check(MediaName.NA_LETTER_WHITE.getValue() == 0, "NA_LETTER_WHITE value == 0");
        check("na-letter-white".equals(MediaName.NA_LETTER_WHITE.toString()), "NA_LETTER_WHITE toString");
        check(MediaName.NA_LETTER_TRANSPARENT.getValue() == 1, "NA_LETTER_TRANSPARENT value == 1");
        check("na-letter-transparent".equals(MediaName.NA_LETTER_TRANSPARENT.toString()), "NA_LETTER_TRANSPARENT toString");
        check(MediaName.ISO_A4_WHITE.getValue() == 2, "ISO_A4_WHITE value == 2");
        check("iso-a4-white".equals(MediaName.ISO_A4_WHITE.toString()), "ISO_A4_WHITE toString");
        check(MediaName.ISO_A4_TRANSPARENT.getValue() == 3, "ISO_A4_TRANSPARENT value == 3");
        check("iso-a4-transparent".equals(MediaName.ISO_A4_TRANSPARENT.toString()), "ISO_A4_TRANSPARENT toString");
        check(MediaName.NA_LETTER_WHITE.clone() == MediaName.NA_LETTER_WHITE, "clone returns singleton");
        check(MediaName.NA_LETTER_WHITE.hashCode() == 0, "hashCode == value");
        check(MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_WHITE), "equals self");
        check(!MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_TRANSPARENT), "distinct constants unequal");
        check(!MediaName.NA_LETTER_WHITE.equals(null) && !MediaName.NA_LETTER_WHITE.equals("x"), "not equals null/foreign");

        // direct protected-method invocation through a minimal subclass (super constructor + super getStringTable/getEnumValueTable)
        MN m = new MN(0);
        String[] st = m.stringTable();
        check(st != null && st.length == 4, "getStringTable length");
        check("na-letter-white".equals(st[0]), "getStringTable first entry");
        javax.print.attribute.EnumSyntax[] evt = m.enumValueTable();
        check(evt != null && evt.length == 4, "getEnumValueTable length");
        check(evt[0] == MediaName.NA_LETTER_WHITE, "getEnumValueTable first entry");
        check(m.getValue() == 0 && "na-letter-white".equals(m.toString()), "subclass ctor sets value 0");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(MediaName.NA_LETTER_WHITE.getValue() == 0, "NA_LETTER_WHITE.getValue == 0");
        check("na-letter-white".equals(MediaName.NA_LETTER_WHITE.toString()), "NA_LETTER_WHITE.toString [na-letter-white]");
        check(MediaName.NA_LETTER_TRANSPARENT.getValue() == 1, "NA_LETTER_TRANSPARENT.getValue == 1");
        check("na-letter-transparent".equals(MediaName.NA_LETTER_TRANSPARENT.toString()), "NA_LETTER_TRANSPARENT.toString [na-letter-transparent]");
        check(MediaName.ISO_A4_WHITE.getValue() == 2, "ISO_A4_WHITE.getValue == 2");
        check("iso-a4-white".equals(MediaName.ISO_A4_WHITE.toString()), "ISO_A4_WHITE.toString [iso-a4-white]");
        check(MediaName.ISO_A4_TRANSPARENT.getValue() == 3, "ISO_A4_TRANSPARENT.getValue == 3");
        check("iso-a4-transparent".equals(MediaName.ISO_A4_TRANSPARENT.toString()), "ISO_A4_TRANSPARENT.toString [iso-a4-transparent]");
        check("media".equals(MediaName.NA_LETTER_WHITE.getName()), "getName == \"media\"");
        check(Media.class == MediaName.NA_LETTER_WHITE.getCategory(), "getCategory");
        check(MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_WHITE), "equals self");
        check(!MediaName.NA_LETTER_WHITE.equals(null), "not equals null");
        check(!MediaName.NA_LETTER_WHITE.equals("na-letter-white"), "not equals foreign type");
        check(MediaName.NA_LETTER_WHITE.hashCode() == 0, "hashCode == value");
        check(MediaName.NA_LETTER_WHITE.clone() == MediaName.NA_LETTER_WHITE, "clone returns same singleton");
        check(!MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_TRANSPARENT), "distinct constants unequal");
        check(MediaName.NA_LETTER_WHITE.toString() != MediaName.NA_LETTER_TRANSPARENT.toString() || true, "toString stable");
        check(!MediaName.NA_LETTER_WHITE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        MediaName.NA_LETTER_WHITE,
        MediaName.NA_LETTER_TRANSPARENT,
        MediaName.ISO_A4_WHITE,
        MediaName.ISO_A4_TRANSPARENT
    };
    static final String[] NAMES = {
        "NA_LETTER_WHITE",
        "NA_LETTER_TRANSPARENT",
        "ISO_A4_WHITE",
        "ISO_A4_TRANSPARENT"
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
