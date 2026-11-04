/*
 * @test
 * @summary java.awt.DisplayMode conformance: width/height/bit depth/refresh.
 * @run main DisplayModeTest
 */


import java.awt.DisplayMode;
public class DisplayModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        DisplayMode d = new DisplayMode(1920, 1080, 32, 60);
        check(d.getWidth() == 1920 && d.getHeight() == 1080, "width/height");
        check(d.getBitDepth() == 32, "bit depth");
        check(d.getRefreshRate() == 60, "refresh rate");
        check(d.equals(new DisplayMode(1920, 1080, 32, 60)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
