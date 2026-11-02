/*
 * @test
 * @summary java.awt.PageAttributes conformance: color/media/orientation/origin/quality.
 * @run main PageAttributesTest
 */


import java.awt.PageAttributes;
public class PageAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        PageAttributes p = new PageAttributes();
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
        check(p.getPrinterResolution()[0] == 600 && p.getPrinterResolution()[1] == 600, "resolution");
        PageAttributes copy = new PageAttributes(p);
        check(copy.getColor() == PageAttributes.ColorType.COLOR, "copy ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
