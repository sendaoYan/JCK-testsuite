/*
 * @test
 * @summary java.util.BitSet API conformance: set/get/clear/flip, cardinality, length,
 *          nextSetBit/nextClearBit, and bitwise or/and.
 * @run main BitSetTest
 */

import java.util.BitSet;

public class BitSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        BitSet b = new BitSet();
        check(b.isEmpty() && b.length() == 0, "empty");
        b.set(0);
        b.set(2);
        b.set(10);
        check(b.get(0) && b.get(2) && b.get(10) && !b.get(1), "set/get");
        check(b.cardinality() == 3, "cardinality");
        check(b.length() == 11, "length == highest set bit + 1");
        check(b.nextSetBit(0) == 0 && b.nextSetBit(1) == 2 && b.nextSetBit(3) == 10, "nextSetBit");
        check(b.nextClearBit(0) == 1, "nextClearBit");
    }

    static void testClearFlip() {
        BitSet b = new BitSet();
        b.set(0);
        b.set(2);
        b.clear(2);
        check(!b.get(2) && b.cardinality() == 1, "clear");
        b.flip(0);
        check(!b.get(0) && b.cardinality() == 0, "flip");
    }

    static void testBitwise() {
        BitSet x = new BitSet();
        x.set(1);
        BitSet y = new BitSet();
        y.set(1);
        y.set(3);
        x.or(y);
        check(x.get(1) && x.get(3), "or");

        BitSet p = new BitSet();
        p.set(0);
        p.set(1);
        BitSet q = new BitSet();
        q.set(1);
        p.and(q);
        check(!p.get(0) && p.get(1), "and");
    }

    public static void main(String[] args) {
        testBasics();
        testClearFlip();
        testBitwise();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
