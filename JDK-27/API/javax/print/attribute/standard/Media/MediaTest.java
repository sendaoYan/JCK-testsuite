/*
 * @test
 * @summary javax.print.attribute.standard.Media conformance: white-box structure + black-box behavior.
 * @run main MediaTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Media");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Media m = javax.print.attribute.standard.MediaName.ISO_A4_WHITE;
        check(m.getCategory() == Media.class, "getCategory");
        check("media".equals(m.getName()), "getName");
        check(m.getValue() >= 0, "value non-negative");
        check(m.clone() == m, "EnumSyntax clone returns singleton");
        check(m.hashCode() == m.getValue(), "hashCode == value");
        check(m.equals(m) && !m.equals(javax.print.attribute.standard.MediaName.NA_LETTER_WHITE), "identity equals");
        check(m.toString() != null && m.toString().length() > 0, "toString non-empty");
        MediaSizeName n = javax.print.attribute.standard.MediaSizeName.ISO_A4;
        check(Media.class.isInstance(n) && "media".equals(n.getName()), "subclass is-a Media");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Media union semantics / equality across MediaName+MediaTray+MediaSizeName ----
    static void testContractDeep() throws Exception {
        check(javax.print.attribute.standard.MediaName.ISO_A4_WHITE.getCategory() == javax.print.attribute.standard.Media.class, "MediaName category is Media");
        check(javax.print.attribute.standard.MediaTray.MAIN.getCategory() == javax.print.attribute.standard.Media.class, "MediaTray category is Media");
        check(javax.print.attribute.standard.MediaSizeName.ISO_A4.getCategory() == javax.print.attribute.standard.Media.class, "MediaSizeName category is Media");
        check("media".equals(javax.print.attribute.standard.MediaName.ISO_A4_WHITE.getName()), "MediaName name media");
        check("media".equals(javax.print.attribute.standard.MediaTray.MAIN.getName()), "MediaTray name media");
        check("media".equals(javax.print.attribute.standard.MediaSizeName.ISO_A4.getName()), "MediaSizeName name media");
        check(!javax.print.attribute.standard.MediaName.ISO_A4_WHITE.equals(javax.print.attribute.standard.MediaTray.MAIN), "cross Media subtype unequal (class check)");
        check(!javax.print.attribute.standard.MediaSizeName.ISO_A4.equals(javax.print.attribute.standard.MediaName.ISO_A4_WHITE), "MediaSizeName vs MediaName unequal");
        check(javax.print.attribute.standard.MediaName.ISO_A4_WHITE.getValue() == 2 && javax.print.attribute.standard.MediaTray.MAIN.getValue() == 6 && javax.print.attribute.standard.MediaSizeName.ISO_A4.getValue() == 4, "independent value spaces");
        check(javax.print.attribute.standard.MediaName.NA_LETTER_WHITE.toString().equals("na-letter-white"), "MediaName toString");
        check(javax.print.attribute.standard.MediaTray.LARGE_CAPACITY.toString().equals("large-capacity"), "MediaTray toString");
        check(javax.print.attribute.standard.MediaSizeName.NA_LETTER.toString().equals("na-letter"), "MediaSizeName toString");
        check(serRoundTrip(javax.print.attribute.standard.MediaTray.SIDE) == javax.print.attribute.standard.MediaTray.SIDE, "MediaTray serialization singleton");
        check(serRoundTrip(javax.print.attribute.standard.MediaName.NA_LETTER_TRANSPARENT) == javax.print.attribute.standard.MediaName.NA_LETTER_TRANSPARENT, "MediaName serialization singleton");
        check(serRoundTrip(javax.print.attribute.standard.MediaSizeName.JIS_B5) == javax.print.attribute.standard.MediaSizeName.JIS_B5, "MediaSizeName serialization singleton");
        check(javax.print.attribute.standard.MediaTray.TOP.equals(javax.print.attribute.standard.MediaTray.TOP) && !javax.print.attribute.standard.MediaTray.TOP.equals(javax.print.attribute.standard.MediaTray.BOTTOM), "same-class equals");
        check(javax.print.attribute.standard.MediaTray.TOP.hashCode() == 0, "EnumSyntax hashCode == value");
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
