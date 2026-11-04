/*
 * @test
 * @summary javax.swing.SizeSequence conformance: white-box structure + black-box behavior.
 * @run main SizeSequenceTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SizeSequence;

public class SizeSequenceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SizeSequence");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SizeSequence seq = new SizeSequence();
        seq.insertEntries(0, 3, 10); // [10,10,10]
        check(seq.getSize(0) == 10 && seq.getSize(2) == 10, "insertEntries");
        check(seq.getPosition(1) == 10, "getPosition");
        check(seq.getIndex(15) == 1, "getIndex");
        seq.setSize(1, 20); // [10,20,10]
        check(seq.getSize(1) == 20, "setSize");
        check(seq.getPosition(2) == 30, "position after resize");
        int[] sizes = seq.getSizes();
        check(sizes.length == 3 && sizes[1] == 20, "getSizes");
        seq.removeEntries(0, 1); // [20,10]
        check(seq.getSize(0) == 20, "removeEntries");

        SizeSequence seq2 = new SizeSequence(5, 100);
        check(seq2.getSize(0) == 100 && seq2.getSize(4) == 100, "ctor count+value");
        SizeSequence seq3 = new SizeSequence(new int[] { 1, 2, 3 });
        check(seq3.getSize(2) == 3, "ctor int[]");
        seq3.setSizes(new int[] { 7, 8, 9 });
        check(seq3.getSize(0) == 7, "setSizes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
