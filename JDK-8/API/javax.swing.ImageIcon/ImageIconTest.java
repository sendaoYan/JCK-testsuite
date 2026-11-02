/*
 * @test
 * @summary javax.swing.ImageIcon conformance: empty icon (headless-safe).
 * @run main ImageIconTest
 */

import javax.swing.ImageIcon;

public class ImageIconTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ImageIcon ic = new ImageIcon();
        check(ic.getImage() == null, "no image");
        check(ic.getIconWidth() == -1, "width -1");
        check(ic.getIconHeight() == -1, "height -1");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
