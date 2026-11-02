/*
 * @test
 * @summary java.awt.font.FontRenderContext conformance: transform and hints.
 * @run main FontRenderContextTest
 */


import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
public class FontRenderContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        check(frc.isAntiAliased(), "antialiased");
        check(frc.usesFractionalMetrics(), "fractional metrics");
        check(!frc.isTransformed(), "identity not transformed");
        check(frc.getTransform().isIdentity(), "identity transform");
        FontRenderContext frc2 = new FontRenderContext(new AffineTransform(2, 0, 0, 2, 0, 0), false, false);
        check(frc2.isTransformed(), "is transformed");
        check(!frc2.isAntiAliased(), "not antialiased");
        check(frc.equals(new FontRenderContext(new AffineTransform(), true, true)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
