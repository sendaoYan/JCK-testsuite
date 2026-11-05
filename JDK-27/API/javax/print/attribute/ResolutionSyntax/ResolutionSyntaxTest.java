/*
 * @test
 * @summary javax.print.attribute.ResolutionSyntax conformance: white-box structure + black-box behavior.
 * @run main ResolutionSyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ResolutionSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.ResolutionSyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ResolutionSyntax r = new PrinterResolution(300, 600, ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(ResolutionSyntax.DPI) == 300 && r.getFeedResolution(ResolutionSyntax.DPI) == 600, "getCrossFeed/getFeed");
        check("30000x60000 dphi".equals(r.toString()), "toString");
        check(r.lessThanOrEquals(new PrinterResolution(600, 600, ResolutionSyntax.DPI)), "lessThanOrEquals");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: direct instantiation / dru normalization / physical equals / lte ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.ResolutionSyntax r = new MyRes(300, 600, javax.print.attribute.ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 300, "crossFeed getter");
        check(r.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 600, "feed getter");
        check(r.toString().equals("30000x60000 dphi"), "toString dphi form");
        check(r.equals(new MyRes(150, 300, 200)), "physical equality unit scaling");
        check(r.hashCode() == new MyRes(150, 300, 200).hashCode(), "hashCode physical");
        check(r.lessThanOrEquals(r), "self lte");
        check(!r.lessThanOrEquals(new MyRes(150, 600, javax.print.attribute.ResolutionSyntax.DPI)), "lte false when crossFeed bigger");
        expectIAE("cross 0", new Runnable() { public void run() { new MyRes(0, 1, 100); } }, "crossFeedResolution is < 1");
        expectIAE("feed 0", new Runnable() { public void run() { new MyRes(1, 0, 100); } }, "feedResolution is < 1");
        expectIAE("units 0", new Runnable() { public void run() { new MyRes(1, 1, 0); } }, "units is < 1");
        javax.print.attribute.ResolutionSyntax rt = (javax.print.attribute.ResolutionSyntax) serRoundTrip(r);
        check(rt.equals(r), "serialization round-trip");
        check(!r.equals(null), "null unequal");
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
    static void expectCCE(String what, Runnable r) {
        try { r.run(); throw new AssertionError("expected CCE: " + what); }
        catch (ClassCastException e) { check(e.getMessage() == null, "CCE message null: [" + e.getMessage() + "]"); }
    }

    static class MyRes extends javax.print.attribute.ResolutionSyntax {
        MyRes(int cf, int f, int u) { super(cf, f, u); }
    }

}
