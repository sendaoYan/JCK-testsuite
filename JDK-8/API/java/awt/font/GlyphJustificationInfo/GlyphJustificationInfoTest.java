/*
 * @test
 * @summary java.awt.font.GlyphJustificationInfo conformance: justification metrics.
 * @run main GlyphJustificationInfoTest
 */


import java.awt.font.GlyphJustificationInfo;
public class GlyphJustificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        GlyphJustificationInfo g = new GlyphJustificationInfo(0.5f, true, GlyphJustificationInfo.PRIORITY_INTERCHAR, 0.25f, 0.75f, false, GlyphJustificationInfo.PRIORITY_NONE, 0f, 0f);
        check(g.weight == 0.5f, "weight");
        check(g.growPriority == GlyphJustificationInfo.PRIORITY_INTERCHAR, "grow priority");
        check(g.growLeftLimit == 0.25f && g.growRightLimit == 0.75f, "grow limits");
        check(g.growAbsorb, "grow absorb");
        check(g.shrinkPriority == GlyphJustificationInfo.PRIORITY_NONE, "shrink priority");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
