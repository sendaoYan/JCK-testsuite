/*
 * @test
 * @summary java.awt.font.FontRenderContext conformance: white-box structure plus black-box behavior.
 * @run main FontRenderContextTest
 */

import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.lang.reflect.*;

public class FontRenderContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.FontRenderContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(hasMethod(c, "getTransform"), "getTransform()");
        check(hasMethod(c, "getTransformType"), "getTransformType()");
        check(hasMethod(c, "isTransformed"), "isTransformed()");
        check(hasMethod(c, "isAntiAliased"), "isAntiAliased()");
        check(hasMethod(c, "usesFractionalMetrics"), "usesFractionalMetrics()");
        check(hasMethod(c, "getAntiAliasingHint"), "getAntiAliasingHint()");
        check(hasMethod(c, "getFractionalMetricsHint"), "getFractionalMetricsHint()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AffineTransform id = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(id, false, false);
        check(!frc.isAntiAliased(), "aa=false -> isAntiAliased false");
        check(!frc.usesFractionalMetrics(), "fm=false -> usesFractionalMetrics false");
        check(!frc.isTransformed(), "identity tx -> not transformed");
        check(frc.getTransformType() == AffineTransform.TYPE_IDENTITY, "identity transform type");
        check(frc.getTransform().equals(id), "getTransform equals identity");
        // getTransform returns a defensive copy
        AffineTransform t1 = frc.getTransform();
        AffineTransform t2 = frc.getTransform();
        check(t1 != t2 && t1.equals(t2), "getTransform returns independent copies");

        FontRenderContext aaFrc = new FontRenderContext(id, true, true);
        check(aaFrc.isAntiAliased() && aaFrc.usesFractionalMetrics(), "aa/fm true");

        // object-hint constructor
        FontRenderContext hintFrc = new FontRenderContext(id,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        check(hintFrc.isAntiAliased(), "hint ON -> isAntiAliased");
        check(hintFrc.usesFractionalMetrics(), "hint ON -> usesFractionalMetrics");
        check(hintFrc.getAntiAliasingHint() == RenderingHints.VALUE_TEXT_ANTIALIAS_ON, "getAntiAliasingHint");
        check(hintFrc.getFractionalMetricsHint() == RenderingHints.VALUE_FRACTIONALMETRICS_ON, "getFractionalMetricsHint");

        // scaled transform changes type
        FontRenderContext scaled = new FontRenderContext(AffineTransform.getScaleInstance(2, 2), false, false);
        check(scaled.isTransformed(), "scaled isTransformed");
        check(scaled.getTransformType() == AffineTransform.TYPE_UNIFORM_SCALE, "scaled transform type UNIFORM_SCALE");

        // equals / hashCode
        FontRenderContext a = new FontRenderContext(id, true, false);
        FontRenderContext b = new FontRenderContext(new AffineTransform(), true, false);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equal FRC equal + same hashCode");
        check(!a.equals(new FontRenderContext(id, false, false)), "different aa not equal");
        check(!a.equals(null) && !a.equals("x"), "equals(null)/non-FRC false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
