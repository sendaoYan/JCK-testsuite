/*
 * @test
 * @summary javax.print.attribute.standard.PrinterResolution conformance: resolution attribute.
 * @run main PrinterResolutionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterResolutionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterResolution v = new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI);
        int[] r = v.getResolution(javax.print.attribute.ResolutionSyntax.DPI);
        check(r.length == 2 && r[0] == 300 && r[1] == 600, "getResolution");
        check(v.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 300, "cross feed");
        check(v.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 600, "feed");
        check(v.getCategory() == PrinterResolution.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI)), "equals");
        check(v.hashCode() == new PrinterResolution(300, 600, javax.print.attribute.ResolutionSyntax.DPI).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
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
