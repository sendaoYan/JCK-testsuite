/*
 * @test
 * @summary java.awt.AlphaComposite conformance: rules and alpha.
 * @run main AlphaCompositeTest
 */


import java.awt.AlphaComposite;
public class AlphaCompositeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        AlphaComposite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        check(a.getRule() == AlphaComposite.SRC_OVER, "rule");
        check(a.getAlpha() == 0.5f, "alpha");
        check(a.equals(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)), "equals");
        AlphaComposite def = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        check(def.getAlpha() == 1.0f, "default alpha 1.0");
        check(AlphaComposite.SrcOver.getRule() == AlphaComposite.SRC_OVER, "SrcOver constant");
        check(AlphaComposite.Xor.getRule() == AlphaComposite.XOR, "Xor constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
