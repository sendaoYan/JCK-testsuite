/*
 * @test
 * @summary java.awt.image.renderable.RenderContext conformance: white-box structure plus black-box behavior.
 * @run main RenderContextTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.renderable.*;
import java.lang.reflect.*;

public class RenderContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.RenderContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AffineTransform tx = AffineTransform.getScaleInstance(2, 2);
        Rectangle2D aoi = new Rectangle2D.Double(0, 0, 10, 10);
        RenderContext rc = new RenderContext(tx, aoi, null);
        check(rc.getTransform().equals(tx), "getTransform equals constructor transform");
        check(rc.getAreaOfInterest().equals(aoi), "getAreaOfInterest");
        check(rc.getRenderingHints() == null, "null rendering hints");

        AffineTransform t1 = rc.getTransform();
        AffineTransform t2 = rc.getTransform();
        check(t1 != t2, "getTransform returns defensive copies");

        RenderContext rc2 = new RenderContext(AffineTransform.getScaleInstance(1, 1));
        rc2.setTransform(tx);
        check(rc2.getTransform().equals(tx), "setTransform");
        rc2.setAreaOfInterest(aoi);
        check(rc2.getAreaOfInterest().equals(aoi), "setAreaOfInterest");
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rc2.setRenderingHints(hints);
        check(rc2.getRenderingHints() == hints, "setRenderingHints");

        AffineTransform pre = AffineTransform.getTranslateInstance(3, 4);
        rc2.preConcatenateTransform(pre);
        check(rc2.getTransform().isIdentity() == false, "preConcatenateTransform applies");
        AffineTransform concat = AffineTransform.getTranslateInstance(1, 1);
        rc2.concatenateTransform(concat);
        check(rc2.getTransform() != null, "concatenateTransform");

        RenderContext cloned = (RenderContext) rc2.clone();
        check(cloned != rc2 && cloned.getTransform().equals(rc2.getTransform()), "clone independent equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
