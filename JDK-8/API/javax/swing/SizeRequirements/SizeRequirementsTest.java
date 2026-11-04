/*
 * @test
 * @summary javax.swing.SizeRequirements conformance: size fields, tiled/aligned aggregation math,
 *          calculateTiledPositions/calculateAlignedPositions exact geometry (headless-safe).
 * @run main SizeRequirementsTest
 */

import java.util.Arrays;

import javax.swing.SizeRequirements;
import javax.swing.UIManager;

public class SizeRequirementsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testFields() {
        SizeRequirements r = new SizeRequirements();
        r.minimum = 10; r.preferred = 20; r.maximum = 30;
        check(r.minimum == 10 && r.preferred == 20 && r.maximum == 30, "fields");
        check(r.alignment == 0.5f, "default ctor alignment 0.5");
        check(r.minimum == 0 || r.minimum == 10, "minimum field public mutable");
        SizeRequirements z = new SizeRequirements();
        check(z.minimum == 0 && z.preferred == 0 && z.maximum == 0, "default ctor zeroes");
        SizeRequirements s = new SizeRequirements(3, 5, 7, 0.25f);
        check(s.minimum == 3 && s.preferred == 5 && s.maximum == 7 && s.alignment == 0.25f,
                "(min,pref,max,align) ctor");
        check(new SizeRequirements(3, 5, 7, 0.5f).toString().equals("[3,5,7]@0.5"), "toString format [3,5,7]@0.5");
    }

    static void testTiledAggregation() {
        // tiled: sizes add along the axis, alignment is the average
        SizeRequirements[] three = {
            new SizeRequirements(3, 5, 7, 0.5f),
            new SizeRequirements(3, 5, 7, 0.5f),
            new SizeRequirements(3, 5, 7, 0.5f),
        };
        SizeRequirements t = SizeRequirements.getTiledSizeRequirements(three);
        check(t.minimum == 9, "tiled minimum 3*3=9");
        check(t.preferred == 15, "tiled preferred 3*5=15");
        check(t.maximum == 21, "tiled maximum 3*7=21");
        check(t.alignment == 0.5f, "tiled alignment 0.5");
        SizeRequirements single = SizeRequirements.getTiledSizeRequirements(
                new SizeRequirements[] { new SizeRequirements(3, 5, 7, 0.5f) });
        check(single.minimum == 3 && single.preferred == 5 && single.maximum == 7,
                "tiled single passes through");
        SizeRequirements mixed = SizeRequirements.getTiledSizeRequirements(
                new SizeRequirements[] { new SizeRequirements(10, 10, 10, 0.5f),
                                         new SizeRequirements(4, 8, 16, 0.5f) });
        check(mixed.minimum == 14 && mixed.preferred == 18 && mixed.maximum == 26,
                "tiled {10, 4/8/16} = 14/18/26");
    }

    static void testAlignedAggregation() {
        // aligned: extent covers all children from the least-aligned left edge to the
        // furthest-aligned right edge; alignment is the weighted centroid
        SizeRequirements[] a = {
            new SizeRequirements(3, 5, 7, 0.5f),
            new SizeRequirements(3, 5, 7, 0.0f),
            new SizeRequirements(3, 5, 7, 1.0f),
        };
        SizeRequirements r = SizeRequirements.getAlignedSizeRequirements(a);
        check(r.minimum == 6, "aligned {0,.5,1} minimum 6");
        check(r.preferred == 10, "aligned {0,.5,1} preferred 10");
        check(r.maximum == 14, "aligned {0,.5,1} maximum 14");
        check(r.alignment == 0.5f, "aligned {0,.5,1} alignment 0.5");
        // a child with a bigger minimum than the others' preferred dominates
        SizeRequirements[] b = {
            new SizeRequirements(10, 10, 10, 0.5f),
            new SizeRequirements(4, 8, 16, 0.5f),
        };
        SizeRequirements r2 = SizeRequirements.getAlignedSizeRequirements(b);
        check(r2.minimum == 10 && r2.preferred == 10 && r2.maximum == 16 && r2.alignment == 0.5f,
                "aligned {10@.5, 4/8/16@.5} = 10/10/16@.5");
        // single child keeps its size but its alignment normalizes against its own extent
        SizeRequirements r3 = SizeRequirements.getAlignedSizeRequirements(
                new SizeRequirements[] { new SizeRequirements(3, 5, 7, 0.5f) });
        check(r3.minimum == 3 && r3.preferred == 5 && r3.maximum == 7,
                "aligned single sizes pass through");
        check(Math.abs(r3.alignment - 1.0f / 3.0f) < 1e-6, "aligned single alignment 1/3");
    }

    static void testCalculateTiledPositions() {
        // exact allocation: 20px distributed over two children preferring 7 each -> 7+7, rest unused
        int[] offs = new int[2];
        int[] spans = new int[2];
        SizeRequirements[] two = {
            new SizeRequirements(7, 7, 7, 0.5f),
            new SizeRequirements(7, 7, 7, 0.5f),
        };
        SizeRequirements.calculateTiledPositions(20, new SizeRequirements(20, 20, 20, 0.5f), two, offs, spans);
        check(Arrays.toString(offs).equals("[0, 7]"), "tiled(20) offsets [0,7]");
        check(Arrays.toString(spans).equals("[7, 7]"), "tiled(20) spans [7,7]");
        // rigid (min=pref=max) children ignore the allocated total entirely
        SizeRequirements.calculateTiledPositions(10, new SizeRequirements(10, 10, 10, 0.5f), two, offs, spans);
        check(Arrays.toString(offs).equals("[0, 7]"), "tiled(10) rigid children still offsets [0,7]");
        check(Arrays.toString(spans).equals("[7, 7]"), "tiled(10) rigid children still spans [7,7]");
        // deficit shrinks shrinkable children proportionally down toward their minimum
        SizeRequirements[] shrinkable = {
            new SizeRequirements(5, 7, 7, 0.5f),
            new SizeRequirements(5, 7, 7, 0.5f),
        };
        SizeRequirements.calculateTiledPositions(10, new SizeRequirements(10, 10, 10, 0.5f), shrinkable, offs, spans);
        check(Arrays.toString(offs).equals("[0, 5]"), "tiled(10 deficit) offsets [0,5]");
        check(Arrays.toString(spans).equals("[5, 5]"), "tiled(10 deficit) spans [5,5]");
        // surplus grows growable children evenly
        SizeRequirements[] growable = {
            new SizeRequirements(7, 7, 9, 0.5f),
            new SizeRequirements(7, 7, 9, 0.5f),
        };
        SizeRequirements.calculateTiledPositions(16, new SizeRequirements(16, 16, 16, 0.5f), growable, offs, spans);
        check(Arrays.toString(offs).equals("[0, 8]"), "tiled(16 surplus) offsets [0,8]");
        check(Arrays.toString(spans).equals("[8, 8]"), "tiled(16 surplus) spans [8,8]");
        // surplus (30) over {10 fixed, 10..30 flexible}: extra goes to the flexible child
        SizeRequirements[] flex = {
            new SizeRequirements(10, 10, 10, 0.5f),
            new SizeRequirements(10, 20, 30, 0.5f),
        };
        SizeRequirements.calculateTiledPositions(30, new SizeRequirements(30, 30, 30, 0.5f), flex, offs, spans);
        check(Arrays.toString(offs).equals("[0, 10]"), "tiled(30) offsets [0,10]");
        check(Arrays.toString(spans).equals("[10, 20]"), "tiled(30) spans [10,20]");
    }

    static void testCalculateAlignedPositions() {
        int[] offs = new int[2];
        int[] spans = new int[2];
        // opposite alignments (0 and 1) in a 30px box: children pushed apart
        SizeRequirements[] ends = {
            new SizeRequirements(7, 7, 7, 0.0f),
            new SizeRequirements(7, 7, 7, 1.0f),
        };
        SizeRequirements.calculateAlignedPositions(30, new SizeRequirements(30, 30, 30, 0.5f), ends, offs, spans);
        check(Arrays.toString(offs).equals("[15, 8]"), "aligned(30,{0,1}) offsets [15,8]");
        check(Arrays.toString(spans).equals("[7, 7]"), "aligned(30,{0,1}) spans [7,7]");
        // both centered in a 20px box: first at 10-10=0? centered pairs land at 10 and 0
        SizeRequirements[] mid = {
            new SizeRequirements(10, 10, 10, 0.0f),
            new SizeRequirements(10, 10, 10, 1.0f),
        };
        SizeRequirements.calculateAlignedPositions(20, new SizeRequirements(20, 20, 20, 0.5f), mid, offs, spans);
        check(Arrays.toString(offs).equals("[10, 0]"), "aligned(20,{0,1}) offsets [10,0]");
        check(Arrays.toString(spans).equals("[10, 10]"), "aligned(20,{0,1}) spans [10,10]");
    }

    static void testAdjustSizesStub() {
        // adjustSizes is a historical stub: it always returns an empty array
        int[] neg = SizeRequirements.adjustSizes(-6, new SizeRequirements[] {
            new SizeRequirements(3, 5, 7, 0.5f), new SizeRequirements(3, 5, 7, 0.5f) });
        check(neg != null && neg.length == 0, "adjustSizes(-6) returns empty array");
        int[] pos = SizeRequirements.adjustSizes(6, new SizeRequirements[] {
            new SizeRequirements(5, 5, 5, 0.5f) });
        check(pos != null && pos.length == 0, "adjustSizes(+6) returns empty array");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testFields();
        testTiledAggregation();
        testAlignedAggregation();
        testCalculateTiledPositions();
        testCalculateAlignedPositions();
        testAdjustSizesStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
