/*
 * @test
 * @summary java.awt.PageAttributes conformance: page attribute bundle (headless-safe):
 *          defaults, every attribute setter/getter, integer encodings for orientation
 *          (3/4) and quality (3/4/5), printer-resolution validation ({x,y,dpi|dpcm}),
 *          null rejection, nested constant-class identity, value equality, clone and
 *          copy-constructor independence, non-serializability.
 * @run main PageAttributesTest
 */

import java.awt.PageAttributes;

public class PageAttributesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static String res(int[] r) { return r[0] + "," + r[1] + "," + r[2]; }

    public static void main(String[] args) throws Exception {
        // Defaults.
        PageAttributes p = new PageAttributes();
        check(p.getColor() == PageAttributes.ColorType.MONOCHROME, "default color monochrome");
        check(p.getMedia() != null, "default media non-null (platform default, e.g. na-letter/a4)");
        check(p.getOrientationRequested() == PageAttributes.OrientationRequestedType.PORTRAIT, "default orientation portrait");
        check(p.getOrigin() == PageAttributes.OriginType.PHYSICAL, "default origin physical");
        check(p.getPrintQuality() == PageAttributes.PrintQualityType.NORMAL, "default quality normal");
        check(res(p.getPrinterResolution()).equals("72,72,3"), "default resolution 72dpi: " + res(p.getPrinterResolution()));

        // Attribute setters and getters.
        p.setColor(PageAttributes.ColorType.COLOR);
        check(p.getColor() == PageAttributes.ColorType.COLOR, "color");
        p.setMedia(PageAttributes.MediaType.A4);
        check(p.getMedia() == PageAttributes.MediaType.A4, "media");
        p.setOrientationRequested(PageAttributes.OrientationRequestedType.LANDSCAPE);
        check(p.getOrientationRequested() == PageAttributes.OrientationRequestedType.LANDSCAPE, "orientation");
        p.setOrigin(PageAttributes.OriginType.PRINTABLE);
        check(p.getOrigin() == PageAttributes.OriginType.PRINTABLE, "origin");
        p.setPrintQuality(PageAttributes.PrintQualityType.HIGH);
        check(p.getPrintQuality() == PageAttributes.PrintQualityType.HIGH, "quality");
        p.setPrinterResolution(600);
        check(res(p.getPrinterResolution()).equals("600,600,3"), "resolution int setter implies dpi");

        // Integer encodings: orientation accepts exactly 3 (portrait) and 4 (landscape).
        p.setOrientationRequested(3);
        check(p.getOrientationRequested() == PageAttributes.OrientationRequestedType.PORTRAIT, "orientation 3 is portrait");
        p.setOrientationRequested(4);
        check(p.getOrientationRequested() == PageAttributes.OrientationRequestedType.LANDSCAPE, "orientation 4 is landscape");
        checkThrows(IllegalArgumentException.class, "orientation 2 rejected",
                new ThrowingRunnable() { public void run() { p.setOrientationRequested(2); } });
        checkThrows(IllegalArgumentException.class, "orientation 5 rejected",
                new ThrowingRunnable() { public void run() { p.setOrientationRequested(5); } });
        // Quality accepts 3 (draft), 4 (normal), 5 (high).
        p.setPrintQuality(3);
        check(p.getPrintQuality() == PageAttributes.PrintQualityType.DRAFT, "quality 3 is draft");
        p.setPrintQuality(4);
        check(p.getPrintQuality() == PageAttributes.PrintQualityType.NORMAL, "quality 4 is normal");
        p.setPrintQuality(5);
        check(p.getPrintQuality() == PageAttributes.PrintQualityType.HIGH, "quality 5 is high");
        checkThrows(IllegalArgumentException.class, "quality 2 rejected",
                new ThrowingRunnable() { public void run() { p.setPrintQuality(2); } });

        // Resolution arrays: {x, y, units} with units 3 (dpi) or 4 (dpcm), positive.
        p.setPrinterResolution(new int[]{72, 144, 4});
        check(res(p.getPrinterResolution()).equals("72,144,4"), "resolution dpcm array");
        final PageAttributes fp = p;
        checkThrows(IllegalArgumentException.class, "resolution bad units",
                new ThrowingRunnable() { public void run() { fp.setPrinterResolution(new int[]{300, 300, 300}); } });
        checkThrows(IllegalArgumentException.class, "resolution wrong arity",
                new ThrowingRunnable() { public void run() { fp.setPrinterResolution(new int[]{300}); } });
        checkThrows(IllegalArgumentException.class, "resolution null",
                new ThrowingRunnable() { public void run() { fp.setPrinterResolution(null); } });
        checkThrows(IllegalArgumentException.class, "resolution negative int",
                new ThrowingRunnable() { public void run() { fp.setPrinterResolution(-5); } });
        // The returned array is a defensive copy.
        int[] got = p.getPrinterResolution();
        got[0] = 1;
        check(p.getPrinterResolution()[0] != 1, "returned resolution array is a copy");

        // Null attribute objects are rejected with IAE.
        checkThrows(IllegalArgumentException.class, "setColor(null)",
                new ThrowingRunnable() { public void run() { fp.setColor(null); } });
        checkThrows(IllegalArgumentException.class, "setMedia(null)",
                new ThrowingRunnable() { public void run() { fp.setMedia(null); } });
        checkThrows(IllegalArgumentException.class, "setOrientationRequested(null)",
                new ThrowingRunnable() { public void run() { fp.setOrientationRequested((PageAttributes.OrientationRequestedType) null); } });
        checkThrows(IllegalArgumentException.class, "setOrigin(null)",
                new ThrowingRunnable() { public void run() { fp.setOrigin(null); } });
        checkThrows(IllegalArgumentException.class, "setPrintQuality(null)",
                new ThrowingRunnable() { public void run() { fp.setPrintQuality(null); } });

        // Nested constant classes: identity singletons, not enums, lowercase names.
        check(!PageAttributes.ColorType.class.isEnum(), "ColorType is a constant class, not an enum");
        check("color".equals(PageAttributes.ColorType.COLOR.toString()), "COLOR name");
        check("monochrome".equals(PageAttributes.ColorType.MONOCHROME.toString()), "MONOCHROME name");
        check(PageAttributes.ColorType.COLOR == PageAttributes.ColorType.COLOR, "COLOR identity");
        check("portrait".equals(PageAttributes.OrientationRequestedType.PORTRAIT.toString()), "PORTRAIT name");
        check("landscape".equals(PageAttributes.OrientationRequestedType.LANDSCAPE.toString()), "LANDSCAPE name");
        check("printable".equals(PageAttributes.OriginType.PRINTABLE.toString())
                && "physical".equals(PageAttributes.OriginType.PHYSICAL.toString()), "OriginType names");
        check("draft".equals(PageAttributes.PrintQualityType.DRAFT.toString())
                && "normal".equals(PageAttributes.PrintQualityType.NORMAL.toString())
                && "high".equals(PageAttributes.PrintQualityType.HIGH.toString()), "PrintQualityType names");
        check("iso-a4".equals(PageAttributes.MediaType.ISO_A4.toString()), "MediaType ISO_A4 name");
        check(PageAttributes.MediaType.ISO_A4.equals(PageAttributes.MediaType.ISO_A4), "MediaType identity equality");

        // Value equality, hashCode, clone.
        PageAttributes e1 = new PageAttributes();
        PageAttributes e2 = new PageAttributes();
        check(e1.equals(e2), "equal defaults");
        check(e1.hashCode() == e2.hashCode(), "hashCode follows equals");
        e2.setColor(PageAttributes.ColorType.COLOR);
        check(!e1.equals(e2), "difference detected");
        check(!e1.equals(null), "equals(null) false");
        e2.setColor(PageAttributes.ColorType.MONOCHROME);
        check(e1.equals(e2), "restored equality");
        PageAttributes cl = (PageAttributes) e1.clone();
        check(cl.equals(e1) && cl != e1, "clone is an equal, distinct object");

        // Copy constructor independence (deep, including arrays).
        PageAttributes src = new PageAttributes();
        src.setPrinterResolution(new int[]{600, 600, 3});
        PageAttributes copy = new PageAttributes(src);
        check(res(copy.getPrinterResolution()).equals("600,600,3"), "copy ctor copies");
        copy.setPrinterResolution(new int[]{300, 300, 3});
        check(res(src.getPrinterResolution()).equals("600,600,3"), "copy ctor is independent");

        // Not serializable.
        boolean failed = false;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(new PageAttributes());
            oos.close();
        } catch (java.io.NotSerializableException expected) {
            failed = true;
        }
        check(failed, "PageAttributes is not serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
