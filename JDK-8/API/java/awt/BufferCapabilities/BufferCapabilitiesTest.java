/*
 * @test
 * @summary java.awt.BufferCapabilities conformance: front/back capabilities and flip.
 * @run main BufferCapabilitiesTest
 */


import java.awt.BufferCapabilities;
import java.awt.ImageCapabilities;
public class BufferCapabilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ImageCapabilities front = new ImageCapabilities(true);
        ImageCapabilities back = new ImageCapabilities(false);
        BufferCapabilities b = new BufferCapabilities(front, back, BufferCapabilities.FlipContents.BACKGROUND);
        check(b.getFrontBufferCapabilities().isAccelerated(), "front accelerated");
        check(!b.getBackBufferCapabilities().isAccelerated(), "back not accelerated");
        check(b.getFlipContents() == BufferCapabilities.FlipContents.BACKGROUND, "flip contents");
        check(b.isPageFlipping(), "page flipping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
