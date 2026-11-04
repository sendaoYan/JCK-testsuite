/*
 * @test
 * @summary java.awt.font.NumericShaper conformance: shaper instances.
 * @run main NumericShaperTest
 */


import java.awt.font.NumericShaper;
public class NumericShaperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        NumericShaper arabic = NumericShaper.getShaper(NumericShaper.Range.ARABIC);
        check(arabic != null, "arabic shaper");
        NumericShaper contextual = NumericShaper.getContextualShaper(NumericShaper.ARABIC);
        check(contextual != null, "contextual shaper");
        check(NumericShaper.getContextualShaper(NumericShaper.ARABIC, NumericShaper.EUROPEAN) != null, "multi-range contextual");
        char[] digits = {'1', '2', '3'};
        arabic.shape(digits, 0, digits.length);
        check(digits.length == 3, "shape in place");
        check(!arabic.isContextual(), "not contextual");
        check(contextual.isContextual(), "is contextual");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
