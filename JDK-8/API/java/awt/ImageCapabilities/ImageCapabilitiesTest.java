/*
 * @test
 * @summary java.awt.ImageCapabilities conformance: accelerated/volatile flags.
 * @run main ImageCapabilitiesTest
 */


import java.awt.ImageCapabilities;
public class ImageCapabilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ImageCapabilities accel = new ImageCapabilities(true);
        check(accel.isAccelerated() && !accel.isTrueVolatile(), "accelerated, not volatile by default");
        ImageCapabilities plain = new ImageCapabilities(false);
        check(!plain.isAccelerated() && !plain.isTrueVolatile(), "plain");
        ImageCapabilities c = (ImageCapabilities) plain.clone();
        check(!c.isAccelerated(), "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
