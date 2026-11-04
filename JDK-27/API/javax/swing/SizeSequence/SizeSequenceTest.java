/*
 * @test
 * @summary javax.swing.SizeSequence conformance: white-box structure + black-box behavior.
 * @run main SizeSequenceTest
 */

import java.util.Arrays;
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


    // ---- Part 3: deep contract coverage ----
    static void testConstructors() {
        // empty sequence reports 0 everywhere
        SizeSequence empty = new SizeSequence();
        check(empty.getSize(0) == 0, "empty getSize(0) == 0");
        check(empty.getSizes().length == 0, "empty getSizes length 0");
        // (n) creates n zero-sized entries
        SizeSequence zeros = new SizeSequence(2);
        check(zeros.getSize(0) == 0 && zeros.getSize(1) == 0, "ctor(n) zero entries");
        check(zeros.getSizes().length == 2, "ctor(n) length n");
        // (n, v) fills every entry with v
        check(new SizeSequence(3, 7).getSize(2) == 7, "ctor(n,7) fills 7");
        // negative entry count explodes inside the backing array allocation
        try {
            new SizeSequence(-1, 5);
            check(false, "ctor(-1,5) should throw NegativeArraySizeException");
        } catch (NegativeArraySizeException expected) {
            check(true, "ctor(-1,5) throws NegativeArraySizeException");
        }
        // array ctor copies the values
        int[] src = {10, 20, 30};
        SizeSequence fromArray = new SizeSequence(src);
        src[0] = 99;
        check(fromArray.getSize(0) == 10, "array ctor copies (immune to later mutation)");
    }

    static void testIndexPositionMapping() {
        SizeSequence s = new SizeSequence(new int[] {10, 20, 30});
        // entries cover [0,10) [10,30) [30,60)
        check(s.getIndex(0) == 0, "getIndex(0) first entry");
        check(s.getIndex(9) == 0, "getIndex(9) still first entry");
        check(s.getIndex(10) == 1, "getIndex(10) second entry");
        check(s.getIndex(29) == 1, "getIndex(29) still second entry");
        check(s.getIndex(30) == 2, "getIndex(30) third entry");
        check(s.getIndex(59) == 2, "getIndex(59) still third entry");
        // a position past the end maps to the entry count
        check(s.getIndex(60) == 3, "getIndex(60) past end == 3");
        check(s.getIndex(61) == 3, "getIndex(61) past end == 3");
        // getPosition(index) returns the start offset of that entry; past-end -> total 60
        check(s.getPosition(0) == 0, "getPosition(0) == 0");
        check(s.getPosition(1) == 10, "getPosition(1) == 10");
        check(s.getPosition(2) == 30, "getPosition(2) == 30");
        check(s.getPosition(3) == 60, "getPosition(3) past end == total 60");
        // negative index does not throw; reports 0
        check(s.getPosition(-1) == 0, "getPosition(-1) == 0 no-throw");
    }

    static void testEntryAlgebra() {
        // insertEntries(1,2,5): two 5-sized entries after index 0
        SizeSequence t = new SizeSequence(new int[] {10, 50, 30});
        t.insertEntries(1, 2, 5);
        int[] after = t.getSizes();
        check(Arrays.equals(after, new int[] {10, 5, 5, 50, 30}), "insertEntries(1,2,5) -> [10,5,5,50,30]");
        check(t.getSize(3) == 50 && t.getSize(4) == 30, "tail entries shifted by insert");
        // removeEntries(0,1) drops the first entry
        t.removeEntries(0, 1);
        check(t.getSizes().length == 4, "removeEntries shrinks length");
        check(t.getSize(0) == 5, "removeEntries(0,1) promotes 5 to front");
        // negative index does not throw (the write lands in the internal prefix-sum padding)
        t.setSize(-1, 5);
        check(true, "setSize(-1,5) no-throw");
        t.getSize(-1);
        check(true, "getSize(-1) no-throw");
        // setSizes replaces the whole sequence and repairs any corruption
        t.setSizes(new int[] {1, 2, 3});
        check(Arrays.equals(t.getSizes(), new int[] {1, 2, 3}), "setSizes/getSizes roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructors();
        testIndexPositionMapping();
        testEntryAlgebra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
