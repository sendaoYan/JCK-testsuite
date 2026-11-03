/*
 * @test
 * @summary java.util.BitSet conformance: white-box structure plus black-box behavior of
 *          set/get/clear/flip, cardinality, length, next/previous set/clear bit, bitwise
 *          and/or/xor/andNot, range operations, valueOf/toByteArray/toLongArray, and clone.
 * @run main BitSetTest
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

public class BitSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.BitSet");
        check(!c.isInterface(), "BitSet is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "BitSet is concrete");
        check(Cloneable.class.isAssignableFrom(c), "BitSet implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "BitSet implements Serializable");
        c.getMethod("set", int.class);
        c.getMethod("clear", int.class);
        c.getMethod("get", int.class);
        c.getMethod("cardinality");
        c.getMethod("nextSetBit", int.class);
        c.getMethod("nextClearBit", int.class);
        c.getMethod("and", BitSet.class);
        c.getMethod("or", BitSet.class);
        c.getMethod("valueOf", long[].class);
        c.getMethod("toByteArray");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testBasics();
        testClearFlip();
        testBitwise();
        testRangesAndPrevious();
        testValueOfRoundTrip();
        testClone();
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
        check(b.size() >= b.length(), "size >= length");
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

        BitSet px = new BitSet();
        px.set(0);
        px.set(1);
        BitSet qx = new BitSet();
        qx.set(1);
        px.xor(qx);
        check(px.get(0) && !px.get(1), "xor");

        BitSet pn = new BitSet();
        pn.set(0);
        pn.set(1);
        pn.andNot(qx);
        check(pn.get(0) && !pn.get(1), "andNot");

        BitSet inter = new BitSet();
        inter.set(1);
        check(inter.intersects(qx), "intersects true");
        check(!new BitSet().intersects(inter), "empty intersects false");
    }

    static void testRangesAndPrevious() {
        BitSet b = new BitSet();
        b.set(2, 5); // bits 2,3,4
        check(b.get(2) && b.get(3) && b.get(4) && !b.get(5), "set(from,to)");
        check(b.cardinality() == 3, "range cardinality");

        BitSet sub = b.get(3, 5);
        check(sub.cardinality() == 2 && sub.get(0) && sub.get(1), "get(from,to) returns shifted BitSet");

        check(b.previousSetBit(3) == 3 && b.previousSetBit(1) == -1, "previousSetBit");
        check(b.previousClearBit(4) == 1, "previousClearBit");

        b.clear(2, 4); // clear 2,3
        check(!b.get(2) && !b.get(3) && b.get(4), "clear(from,to)");
    }

    static void testValueOfRoundTrip() {
        long[] words = { 0b1011L };
        BitSet b = BitSet.valueOf(words);
        check(b.get(0) && b.get(1) && !b.get(2) && b.get(3), "valueOf(long[])");
        check(Arrays.equals(words, b.toLongArray()), "toLongArray round-trip");
        check(BitSet.valueOf(new long[]{0b1011L, 0L}).equals(b), "trailing zero word normalized");

        byte[] bytes = { (byte) 0b10000011 };
        BitSet c = BitSet.valueOf(bytes);
        check(c.get(0) && c.get(1) && c.get(7), "valueOf(byte[]) bit order");
        check(Arrays.equals(bytes, c.toByteArray()), "toByteArray round-trip");
    }

    static void testClone() {
        BitSet b = new BitSet();
        b.set(0);
        b.set(5);
        BitSet clone = (BitSet) b.clone();
        check(clone != b, "clone distinct");
        check(clone.equals(b) && clone.cardinality() == 2, "clone equals original");
        clone.clear(0);
        check(!clone.equals(b), "mutating clone breaks equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
