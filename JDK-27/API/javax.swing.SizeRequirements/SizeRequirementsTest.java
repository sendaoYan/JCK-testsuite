/*
 * @test
 * @summary javax.swing.SizeRequirements conformance: white-box structure + black-box behavior.
 * @run main SizeRequirementsTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SizeRequirements;

public class SizeRequirementsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SizeRequirements");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SizeRequirements sr = new SizeRequirements();
        sr.minimum = 0; sr.preferred = 10; sr.maximum = 100; sr.alignment = 0.5f;
        check(sr.minimum == 0 && sr.preferred == 10 && sr.maximum == 100, "public fields");

        SizeRequirements sr2 = new SizeRequirements(1, 2, 3, 0.5f);
        check(sr2.minimum == 1 && sr2.preferred == 2 && sr2.maximum == 3, "ctor fields");
        check(sr2.alignment == 0.5f, "ctor alignment");
        check(sr2.toString().length() > 0, "toString");

        SizeRequirements[] reqs = { new SizeRequirements(10, 10, 10, 0.5f) };
        SizeRequirements tiled = SizeRequirements.getTiledSizeRequirements(reqs);
        check(tiled != null && tiled.preferred == 10, "tiled");
        SizeRequirements aligned = SizeRequirements.getAlignedSizeRequirements(reqs);
        check(aligned != null && aligned.preferred == 10, "aligned");

        int[] offsets = new int[1];
        int[] spans = new int[1];
        SizeRequirements.calculateTiledPositions(10, tiled, reqs, offsets, spans);
        check(spans[0] == 10, "calculateTiledPositions");
        SizeRequirements.calculateAlignedPositions(10, aligned, reqs, offsets, spans);
        check(spans[0] == 10, "calculateAlignedPositions");
        check(SizeRequirements.adjustSizes(10, reqs).length == 0, "adjustSizes stub returns empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
