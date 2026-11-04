/*
 * @test
 * @summary java.awt.image.BufferedImage conformance: pixel get/set.
 * @run main BufferedImageTest
 */


import java.awt.image.BufferedImage;
public class BufferedImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
        check(img.getWidth() == 10 && img.getHeight() == 20, "dimensions");
        check(img.getType() == BufferedImage.TYPE_INT_ARGB, "type");
        img.setRGB(1, 2, 0x11223344);
        check(img.getRGB(1, 2) == 0x11223344, "set/get RGB");
        check(img.getRGB(0, 0) == 0, "default zero");
        BufferedImage sub = img.getSubimage(0, 0, 5, 5);
        check(sub.getWidth() == 5 && sub.getHeight() == 5, "subimage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
