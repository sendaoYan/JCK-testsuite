/*
 * @test
 * @summary java.awt.BufferCapabilities conformance: white-box structure + black-box behavior.
 * @run main BufferCapabilitiesTest
 */

import java.lang.reflect.*;
import java.awt.BufferCapabilities;
import java.awt.ImageCapabilities;

public class BufferCapabilitiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.BufferCapabilities", false, BufferCapabilitiesTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.BufferCapabilities");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && !Modifier.isAbstract(m), "public concrete class");
        check(c.getEnumConstants() == null, "non-enum");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(c.getConstructor(ImageCapabilities.class, ImageCapabilities.class, BufferCapabilities.FlipContents.class) != null,
              "ctor (front,back,flip)");
        for (String n : new String[] {"getFrontBufferCapabilities", "getBackBufferCapabilities",
                "isPageFlipping", "getFlipContents", "isFullScreenRequired", "isMultiBufferAvailable", "clone"})
            check(c.getMethod(n) != null, "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ImageCapabilities front = new ImageCapabilities(true);
        ImageCapabilities back = new ImageCapabilities(true);
        BufferCapabilities bc = new BufferCapabilities(front, back, BufferCapabilities.FlipContents.BACKGROUND);

        check(bc.getFrontBufferCapabilities() == front, "front caps identity");
        check(bc.getBackBufferCapabilities() == back, "back caps identity");
        check(bc.getFlipContents() == BufferCapabilities.FlipContents.BACKGROUND, "flip contents identity");
        check(bc.isPageFlipping(), "page flipping when flipContents != null");
        check(!bc.isFullScreenRequired(), "full-screen not required");
        check(!bc.isMultiBufferAvailable(), "multi-buffer not available");

        // no flipContents -> no page flipping
        BufferCapabilities noFlip = new BufferCapabilities(front, back, null);
        check(noFlip.getFlipContents() == null, "null flip contents");
        check(!noFlip.isPageFlipping(), "not page flipping when flipContents == null");

        // clone is shallow (same component references) but distinct container
        Object cloned = bc.clone();
        check(cloned instanceof BufferCapabilities, "clone instanceof BufferCapabilities");
        check(cloned != bc, "clone distinct object");
        BufferCapabilities bcc = (BufferCapabilities) cloned;
        check(bcc.getFrontBufferCapabilities() == front, "clone shares front caps");
        check(bcc.getBackBufferCapabilities() == back, "clone shares back caps");
        check(bcc.getFlipContents() == BufferCapabilities.FlipContents.BACKGROUND, "clone shares flip contents");
        check(bcc.isPageFlipping() == bc.isPageFlipping(), "clone preserves isPageFlipping");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
