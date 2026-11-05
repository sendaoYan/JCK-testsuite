/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize conformance: media size class.
 * @run main MediaSizeTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaSizeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaSize v = new MediaSize(210f, 297f, MediaSize.MM);
        check(Math.abs(v.getX(MediaSize.MM) - 210f) < 0.001f, "getX");
        check(Math.abs(v.getY(MediaSize.MM) - 297f) < 0.001f, "getY");
        check(MediaSize.ISO.A4.getMediaSizeName() == MediaSizeName.ISO_A4, "ISO_A4 name");
        check(MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4) != null, "getMediaSizeForName");
        check(v.getCategory() == MediaSize.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new MediaSize(210f, 297f, MediaSize.MM)), "equals");
        check(v.hashCode() == new MediaSize(210f, 297f, MediaSize.MM).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: exact size tables / findMedia nearest / unit conversion / IAE ----
    static void testContractDeep() throws Exception {
        check(MediaSize.ISO.A4.getX(MediaSize.MM) == 210f && MediaSize.ISO.A4.getY(MediaSize.MM) == 297f, "ISO.A4 210x297 mm");
        check(MediaSize.ISO.A3.getX(MediaSize.MM) == 297f && MediaSize.ISO.A3.getY(MediaSize.MM) == 420f, "ISO.A3 297x420 mm");
        check(MediaSize.ISO.A0.getX(MediaSize.MM) == 841f && MediaSize.ISO.A0.getY(MediaSize.MM) == 1189f, "ISO.A0 exact");
        check(MediaSize.ISO.B5.getX(MediaSize.MM) == 176f && MediaSize.ISO.B5.getY(MediaSize.MM) == 250f, "ISO.B5 exact");
        check(MediaSize.ISO.C5.getX(MediaSize.MM) == 162f, "ISO.C5 exact");
        check(MediaSize.NA.LETTER.getX(MediaSize.INCH) == 8.5f && MediaSize.NA.LETTER.getY(MediaSize.INCH) == 11f, "NA.LETTER 8.5x11 in");
        check(MediaSize.NA.LEGAL.getX(MediaSize.INCH) == 8.5f && MediaSize.NA.LEGAL.getY(MediaSize.INCH) == 14f, "NA.LEGAL 8.5x14 in");
        check(Math.abs(MediaSize.NA.LETTER.getX(MediaSize.MM) - 215.9f) < 1e-5f, "NA.LETTER 215.9 mm");
        check(MediaSize.JIS.B5.getX(MediaSize.MM) == 182f && MediaSize.JIS.B5.getY(MediaSize.MM) == 257f, "JIS.B5 182x257 mm");
        check(MediaSize.Engineering.A.getX(MediaSize.INCH) == 8.5f && MediaSize.Engineering.A.getY(MediaSize.INCH) == 11f, "Engineering.A");
        check(MediaSize.Other.EXECUTIVE.getX(MediaSize.INCH) == 7.25f && MediaSize.Other.EXECUTIVE.getY(MediaSize.INCH) == 10.5f, "Other.EXECUTIVE");
        check(MediaSize.ISO.A4.getMediaSizeName() == MediaSizeName.ISO_A4, "ISO.A4 <-> MediaSizeName.ISO_A4");
        check(MediaSize.NA.LETTER.getMediaSizeName() == MediaSizeName.NA_LETTER, "NA.LETTER <-> MediaSizeName.NA_LETTER");
        check(MediaSize.JIS.B5.getMediaSizeName() == MediaSizeName.JIS_B5, "JIS.B5 <-> MediaSizeName.JIS_B5");
        check(new MediaSize(111f, 222f, MediaSize.MM).getMediaSizeName() == null, "unregistered custom size has null MediaSizeName");
        check(MediaSize.findMedia(210f, 297f, MediaSize.MM) == MediaSizeName.ISO_A4, "findMedia exact A4");
        check(MediaSize.findMedia(211f, 298f, MediaSize.MM) == MediaSizeName.ISO_A4, "findMedia nearest A4");
        check(MediaSize.findMedia(111f, 222f, MediaSize.MM) == null, "findMedia unknown size null");
        float[] sz = MediaSize.ISO.A4.getSize(MediaSize.MM);
        check(sz.length == 2 && sz[0] == 210f && sz[1] == 297f, "getSize [w,h] mm");
        float[] si = MediaSize.ISO.A4.getSize(MediaSize.INCH);
        check(Math.abs(si[0] - 8.27f) < 0.02f && Math.abs(si[1] - 11.69f) < 0.02f, "getSize inch approx");
        check(MediaSize.ISO.A4.getX(MediaSize.MM) == MediaSize.ISO.A4.getSize(MediaSize.MM)[0], "getX consistent with getSize[0]");
        check(MediaSize.ISO.A4.equals(new MediaSize(210f, 297f, MediaSize.MM)), "equals same size (no name)");
        check(!MediaSize.ISO.A4.equals(new MediaSize(210f, 298f, MediaSize.MM)), "different size unequal");
        check(MediaSize.ISO.A4.hashCode() == new MediaSize(210f, 297f, MediaSize.MM).hashCode(), "hashCode consistent");
        expectIAE("negative size", new Runnable() { public void run() { new MediaSize(-1f, 100f, MediaSize.MM); } }, "x < 0");
        check(new MediaSize(0f, 100f, MediaSize.MM).getX(MediaSize.MM) == 0f, "zero accepted (only negative rejected)");
        expectIAE("units 0", new Runnable() { public void run() { MediaSize.ISO.A4.getX(0); } }, "units is < 1");
        check("210.0x297.0  mm".equals(MediaSize.ISO.A4.toString(MediaSize.MM, " mm")), "toString(units,suffix) format");
        check("media-size".equals(MediaSize.ISO.A4.getName()), "getName");
        check(MediaSize.class == MediaSize.ISO.A4.getCategory(), "getCategory");
        MediaSize rt = (MediaSize) serRoundTrip(MediaSize.ISO.A4);
        check(rt.equals(MediaSize.ISO.A4), "serialization round-trip");
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
