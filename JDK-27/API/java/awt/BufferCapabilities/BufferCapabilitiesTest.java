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
        testCapsContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static void testCapsContract() {
        // ImageCapabilities: boolean round-trip
        java.awt.ImageCapabilities acc = new java.awt.ImageCapabilities(true);
        check(acc.isAccelerated(), "isAccelerated true ctor");
        check(!acc.isTrueVolatile(), "isTrueVolatile false default");
        java.awt.ImageCapabilities notAcc = new java.awt.ImageCapabilities(false);
        check(!notAcc.isAccelerated(), "isAccelerated false ctor");
        // ImageCapabilities does not override equals (identity semantics) - observed
        check(!acc.equals(new java.awt.ImageCapabilities(true)), "no equals override: identity semantics");
        check(!acc.equals(notAcc), "different instances never equal");
        check(!acc.equals(null), "equals(null) false");
        check(!acc.equals("accelerated"), "equals unrelated type false");
        check(acc.toString().length() > 0, "toString non-empty");
        check(!(java.io.Serializable.class.isAssignableFrom(java.awt.ImageCapabilities.class)),
              "ImageCapabilities not Serializable");
        // BufferCapabilities ctor validation: NPE on null caps
        java.awt.ImageCapabilities ic = new java.awt.ImageCapabilities(true);
        // javadoc says NPE but implementation throws IAE - locked by observation
        boolean badCap = false;
        try { new java.awt.BufferCapabilities(null, ic, null); } catch (java.lang.IllegalArgumentException e) { badCap = true; }
        check(badCap, "null front caps -> IAE (observed, javadoc says NPE)");
        badCap = false;
        try { new java.awt.BufferCapabilities(ic, null, null); } catch (java.lang.IllegalArgumentException e) { badCap = true; }
        check(badCap, "null back caps -> IAE (observed, javadoc says NPE)");
        // non-flipping caps
        java.awt.BufferCapabilities bc = new java.awt.BufferCapabilities(ic, ic, null);
        check(!bc.isPageFlipping(), "no flip contents -> isPageFlipping false");
        check(bc.getFlipContents() == null, "getFlipContents null");
        check(bc.getFrontBufferCapabilities() == ic, "front caps identity");
        check(bc.getBackBufferCapabilities() == ic, "back caps identity");
        check(!bc.isFullScreenRequired(), "isFullScreenRequired false");
        check(!bc.isMultiBufferAvailable(), "isMultiBufferAvailable false");
        check(bc.clone() != null && bc.clone() != bc, "clone returns new object");
        // BufferCapabilities does not override equals (identity semantics) - observed
        check(!bc.equals(new java.awt.BufferCapabilities(ic, ic, null)), "no equals override: identity semantics");
        check(!bc.equals(null), "equals(null) false");
        check(bc.equals(bc), "equals self true");
        // flipping with each FlipContents constant
        java.awt.BufferCapabilities.FlipContents[] fc =
                {java.awt.BufferCapabilities.FlipContents.UNDEFINED,
                 java.awt.BufferCapabilities.FlipContents.BACKGROUND,
                 java.awt.BufferCapabilities.FlipContents.PRIOR,
                 java.awt.BufferCapabilities.FlipContents.COPIED};
        String[] fn = {"UNDEFINED","BACKGROUND","PRIOR","COPIED"};
        for (int i = 0; i < fc.length; i++) {
            java.awt.BufferCapabilities b2 = new java.awt.BufferCapabilities(ic, ic, fc[i]);
            check(b2.isPageFlipping(), fn[i] + ": isPageFlipping true");
            check(b2.getFlipContents() == fc[i], fn[i] + ": getFlipContents identity");
        }
        // FlipContents toString is the lowercase name (locked observed)
        check(java.awt.BufferCapabilities.FlipContents.BACKGROUND.toString().equals("background"),
              "FlipContents BACKGROUND toString");
        check(java.awt.BufferCapabilities.FlipContents.COPIED.toString().equals("copied"),
              "FlipContents COPIED toString");
    }
}
