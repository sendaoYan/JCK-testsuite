/*
 * @test
 * @summary javax.print.attribute.standard.PrinterResolution conformance: white-box structure + black-box behavior.
 * @run main PrinterResolutionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterResolutionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterResolution");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(javax.print.attribute.ResolutionSyntax.class.isAssignableFrom(c), "extends ResolutionSyntax");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrinterResolution r = new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 300 && r.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 600, "getCrossFeed/getFeed");
        int[] v = r.getResolution(javax.print.attribute.ResolutionSyntax.DPI);
        check(v.length == 2 && v[0] == 300 && v[1] == 600, "getResolution");
        check("300x600 dpi".equals(r.toString(javax.print.attribute.ResolutionSyntax.DPI, "dpi")), "toString(DPI)");
        check("30000x60000 dphi".equals(r.toString()), "toString default");
        check("printer-resolution".equals(r.getName()) && r.getCategory() == PrinterResolution.class, "getCategory/getName");
        check(r.lessThanOrEquals(new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI)), "lessThanOrEquals equal");
        check(r.lessThanOrEquals(new PrinterResolution(600, 600, javax.print.attribute.ResolutionSyntax.DPI)), "lessThanOrEquals larger");
        check(!r.lessThanOrEquals(new PrinterResolution(300, 300, javax.print.attribute.ResolutionSyntax.DPI)), "not lessThanOrEquals smaller feed");
        check(r.equals(new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI)), "equals");
        check(!r.equals(new PrinterResolution(300, 300, javax.print.attribute.ResolutionSyntax.DPI)), "different unequal");
        check(!r.equals(null) && !r.equals("x"), "not equals null/foreign");
        boolean threw = false; try { new PrinterResolution(0, 600, javax.print.attribute.ResolutionSyntax.DPI); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero crossfeed IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: (crossFeed,feed) arg order / dRU normalization / physical equality / lte / IAE ----
    static void testContractDeep() throws Exception {
        PrinterResolution r = new PrinterResolution(200, 100, javax.print.attribute.ResolutionSyntax.DPI);
        check(r.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 100, "feed is 2nd ctor arg");
        check(r.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 200, "crossFeed is 1st ctor arg");
        check(r.toString().equals("20000x10000 dphi"), "toString dru form [" + r.toString() + "]");
        check(r.toString(javax.print.attribute.ResolutionSyntax.DPCM, " dpcm").equals("79x39  dpcm"), "toString dpcm form");
        check(javax.print.attribute.ResolutionSyntax.DPI == 100 && javax.print.attribute.ResolutionSyntax.DPCM == 254, "unit constants");
        PrinterResolution half = new PrinterResolution(100, 50, 200);
        check(r.equals(half), "physical equality across unit scalings");
        check(r.hashCode() == half.hashCode(), "hashCode physical");
        check(r.getFeedResolution(200) == 50, "feed in custom units");
        check(half.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 100, "half feed == 100 dpi");
        check(r.lessThanOrEquals(r), "self lte");
        check(r.lessThanOrEquals(half), "equal lte");
        check(new PrinterResolution(100, 100, javax.print.attribute.ResolutionSyntax.DPI).lessThanOrEquals(r), "weaker lte");
        check(!r.lessThanOrEquals(new javax.print.attribute.standard.PrinterResolution(100, 300, javax.print.attribute.ResolutionSyntax.DPI)), "not lte when crossFeed smaller");
        check(!r.equals(new PrinterResolution(200, 101, javax.print.attribute.ResolutionSyntax.DPI)), "different feed unequal");
        check(!r.equals(null) && !r.equals("200x100"), "null/foreign unequal");
        check("printer-resolution".equals(r.getName()), "getName");
        check(PrinterResolution.class == r.getCategory(), "getCategory");
        expectIAE("crossFeed 0", new Runnable() { public void run() { new PrinterResolution(0, 100, javax.print.attribute.ResolutionSyntax.DPI); } }, "crossFeedResolution is < 1");
        expectIAE("feed 0", new Runnable() { public void run() { new PrinterResolution(100, 0, javax.print.attribute.ResolutionSyntax.DPI); } }, "feedResolution is < 1");
        expectIAE("units 0", new Runnable() { public void run() { new PrinterResolution(100, 100, 0); } }, "units is < 1");
        expectIAE("units -1", new Runnable() { public void run() { new PrinterResolution(100, 100, -1); } }, "units is < 1");
        expectIAE("getter units 0", new Runnable() { public void run() { r.getFeedResolution(0); } }, ": units is < 1");
        PrinterResolution rt = (PrinterResolution) serRoundTrip(r);
        check(rt.equals(r), "serialization round-trip");
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
