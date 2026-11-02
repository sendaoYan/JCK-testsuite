/*
 * @test
 * @summary java.awt.font.TextHitInfo conformance: character index and edge.
 * @run main TextHitInfoTest
 */


import java.awt.font.TextHitInfo;
public class TextHitInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        TextHitInfo leading = TextHitInfo.leading(3);
        check(leading.getCharIndex() == 3, "leading char index");
        check(leading.isLeadingEdge(), "leading edge");
        TextHitInfo trailing = TextHitInfo.trailing(3);
        check(trailing.getCharIndex() == 3 && !trailing.isLeadingEdge(), "trailing edge");
        TextHitInfo before = TextHitInfo.beforeOffset(2);
        check(before.getInsertionIndex() == 2, "beforeOffset insertion");
        TextHitInfo after = TextHitInfo.afterOffset(2);
        check(after.getInsertionIndex() == 2, "afterOffset insertion");
        check(leading.equals(TextHitInfo.leading(3)), "equals");
        check(!leading.equals(trailing), "not equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
