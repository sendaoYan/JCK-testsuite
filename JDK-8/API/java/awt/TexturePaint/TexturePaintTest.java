/*
 * @test
 * @summary java.awt.TexturePaint conformance: image and anchor rect.
 * @run main TexturePaintTest
 */


import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
public class TexturePaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Rectangle r = new Rectangle(0, 0, 4, 4);
        TexturePaint t = new TexturePaint(img, r);
        check(t.getImage() == img, "image");
        check(t.getAnchorRect().equals(r), "anchor rect");
        check(t.getTransparency() == java.awt.Transparency.OPAQUE, "opaque");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
