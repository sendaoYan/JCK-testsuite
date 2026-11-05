/*
 * @test
 * @summary javax.print.attribute.standard.MediaPrintableArea conformance: white-box structure + black-box behavior.
 * @run main MediaPrintableAreaTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaPrintableAreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaPrintableArea");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaPrintableArea p = new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH);
        check(p.getX(MediaPrintableArea.INCH) == 1.0f && p.getY(MediaPrintableArea.INCH) == 1.0f, "getX/getY");
        check(p.getWidth(MediaPrintableArea.INCH) == 2.0f && p.getHeight(MediaPrintableArea.INCH) == 2.0f, "getWidth/getHeight");
        float[] a = p.getPrintableArea(MediaPrintableArea.INCH);
        check(a.length == 4 && a[0] == 1.0f && a[1] == 1.0f && a[2] == 2.0f && a[3] == 2.0f, "getPrintableArea");
        check("(1.0,1.0)->(2.0,2.0)".equals(p.toString(MediaPrintableArea.INCH, null)), "toString(INCH,null)");
        check("(25.4,25.4)->(50.8,50.8)mm".equals(p.toString()), "toString default mm");
        check("media-printable-area".equals(p.getName()) && p.getCategory() == MediaPrintableArea.class, "getCategory/getName");
        check(p.equals(new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH)), "equals");
        check(!p.equals(new MediaPrintableArea(1, 1, 3, 2, MediaPrintableArea.INCH)), "different unequal");
        check(!p.equals(null) && !p.equals("x"), "not equals null/foreign");
        check(p.hashCode() == new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH).hashCode(), "hashCode consistent");
        boolean threw = false; try { new MediaPrintableArea(1, 1, 0, 2, MediaPrintableArea.INCH); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero width IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: unit conversion / cross-unit equality / negative IAE / serialization ----
    static void testContractDeep() throws Exception {
        MediaPrintableArea m = new MediaPrintableArea(25.4f, 50.8f, 101.6f, 203.2f, MediaPrintableArea.MM);
        check(m.getX(MediaPrintableArea.MM) == 25.4f, "getX mm exact");
        check(m.getY(MediaPrintableArea.MM) == 50.8f, "getY mm exact");
        check(m.getWidth(MediaPrintableArea.MM) == 101.6f, "getWidth mm exact");
        check(m.getHeight(MediaPrintableArea.MM) == 203.2f, "getHeight mm exact");
        check(Math.abs(m.getX(MediaPrintableArea.INCH) - 1.0f) < 1e-6f, "getX inch = 1.0");
        check(Math.abs(m.getY(MediaPrintableArea.INCH) - 2.0f) < 1e-6f, "getY inch = 2.0");
        check(Math.abs(m.getWidth(MediaPrintableArea.INCH) - 4.0f) < 1e-6f, "getWidth inch = 4.0");
        check(Math.abs(m.getHeight(MediaPrintableArea.INCH) - 8.0f) < 1e-6f, "getHeight inch = 8.0");
        float[] arr = m.getPrintableArea(MediaPrintableArea.MM);
        check(arr.length == 4 && arr[0] == 25.4f && arr[1] == 50.8f && arr[2] == 101.6f && arr[3] == 203.2f, "getPrintableArea array [x,y,w,h]");
        arr[0] = 999f;
        check(m.getX(MediaPrintableArea.MM) == 25.4f, "getPrintableArea defensive copy");
        check(m.equals(new MediaPrintableArea(1f, 2f, 4f, 8f, MediaPrintableArea.INCH)), "cross-unit physical equality");
        check(m.hashCode() == new MediaPrintableArea(1f, 2f, 4f, 8f, MediaPrintableArea.INCH).hashCode(), "hashCode cross-unit");
        check(!m.equals(new MediaPrintableArea(2f, 2f, 4f, 8f, MediaPrintableArea.INCH)), "different area unequal");
        check("(1.0,2.0)->(4.0,8.0) in".equals(m.toString(MediaPrintableArea.INCH, " in")), "toString(units,name) format");
        check(m.toString() != null && m.toString().endsWith("mm"), "default toString mm");
        check("media-printable-area".equals(m.getName()), "getName");
        check(MediaPrintableArea.class == m.getCategory(), "getCategory");
        expectIAE("mpa negative x", new Runnable() { public void run() { new MediaPrintableArea(-1f, 1f, 10f, 10f, MediaPrintableArea.MM); } }, "0 or negative value argument");
        expectIAE("mpa negative w", new Runnable() { public void run() { new MediaPrintableArea(1f, 1f, 0f, 10f, MediaPrintableArea.MM); } }, "0 or negative value argument");
        expectIAE("mpa int form negative", new Runnable() { public void run() { new MediaPrintableArea(1, 1, 10, -10, MediaPrintableArea.MM); } }, "0 or negative value argument");
        expectIAE("mpa units 0", new Runnable() { public void run() { m.getX(0); } }, "units is < 1");
        expectIAE("ctor units 0", new Runnable() { public void run() { new MediaPrintableArea(1f, 1f, 10f, 10f, 0); } }, "0 or negative value argument");
        MediaPrintableArea rt = (MediaPrintableArea) serRoundTrip(m);
        check(rt.equals(m), "serialization round-trip");
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
