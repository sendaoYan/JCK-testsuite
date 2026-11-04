/*
 * @test
 * @summary javax.swing.SizeSequence conformance: constructors, index/position mapping,
 *          insert/remove entry algebra (headless-safe).
 * @run main SizeSequenceTest
 */

import java.util.Arrays;

import javax.swing.SizeSequence;
import javax.swing.UIManager;

public class SizeSequenceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<?> ex, Runnable r, String what) {
        try {
            r.run();
            throw new AssertionError(what + ": expected " + ex.getSimpleName() + " not thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), what + " type " + ex.getSimpleName());
        }
    }

    static void test() throws Exception {
        SizeSequence s = new SizeSequence(new int[] {10, 20, 30});
        check(s.getSizes().length == 3, "length");
        check(s.getSize(0) == 10 && s.getSize(1) == 20 && s.getSize(2) == 30, "sizes");
        check(s.getIndex(5) == 0, "getIndex 0");
        check(s.getIndex(15) == 1, "getIndex 1");
    }

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
        SizeSequence uniform = new SizeSequence(3, 7);
        check(uniform.getSize(0) == 7 && uniform.getSize(1) == 7 && uniform.getSize(2) == 7, "ctor(n,7) fills 7");
        // (n, 0) equivalent to ctor(n)
        check(new SizeSequence(2, 0).getSize(1) == 0, "ctor(n,0) zeroes");
        // negative entry count explodes inside the backing array allocation
        checkThrows(NegativeArraySizeException.class,
                new Runnable() { public void run() { new SizeSequence(-1, 5); } },
                "ctor(-1,5)");
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

    static void testMutation() {
        SizeSequence s = new SizeSequence(new int[] {10, 20, 30});
        // setSize updates one entry in place
        s.setSize(1, 50);
        check(s.getSize(1) == 50, "setSize(1,50)");
        // negative index does not throw (the write lands in the internal prefix-sum padding)
        s.setSize(-1, 5);
        check(true, "setSize(-1,5) no-throw");
        s.getSize(-1);
        check(true, "getSize(-1) no-throw");
        // setSizes replaces the whole sequence and repairs any corruption
        s.setSizes(new int[] {1, 2, 3});
        check(Arrays.equals(s.getSizes(), new int[] {1, 2, 3}), "setSizes/getSizes roundtrip");

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
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        test();
        testConstructors();
        testIndexPositionMapping();
        testMutation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
