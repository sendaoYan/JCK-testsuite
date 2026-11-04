/*
 * @test
 * @summary java.util.BitSet API conformance (Java 8 surface): complete contract coverage of
 *          constructors (nbits<0 NegativeArraySizeException), set/get/clear/flip single + range
 *          (negative/from>to IndexOutOfBoundsException), next/previous set/clear bit -1 sentinel,
 *          length/isEmpty/intersects/cardinality/size, and/or/xor/andNot, valueOf/toByteArray/
 *          toLongArray round-trips + null NPE, clone independence, equals/hashCode/toString/stream,
 *          and Serializable round-trip.
 * @run main BitSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.BitSet;

public class BitSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void testConstructorNegative() {
        expectThrows(NegativeArraySizeException.class, () -> new BitSet(-1),
                     "BitSet(-1) NegativeArraySizeException");
        check(new BitSet(0).isEmpty(), "BitSet(0) empty");
    }

    static void testIndexOutOfBounds() {
        BitSet b = new BitSet();
        expectThrows(IndexOutOfBoundsException.class, () -> b.set(-1), "set(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.get(-1), "get(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.clear(-1), "clear(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.flip(-1), "flip(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.nextSetBit(-1), "nextSetBit(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.nextClearBit(-1), "nextClearBit(-1) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.previousSetBit(-2), "previousSetBit(-2) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.previousClearBit(-2), "previousClearBit(-2) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.set(5, 2), "set(from>to) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.set(-1, 2), "set(from<0) IOBE");
        expectThrows(IndexOutOfBoundsException.class, () -> b.flip(5, 2), "flip(from>to) IOBE");
    }

    static void testRangeOperations() {
        BitSet b = new BitSet();
        b.set(2, 5); // sets bits 2,3,4
        check(b.get(2) && b.get(3) && b.get(4) && !b.get(5), "set(from,to)");
        check(b.cardinality() == 3, "range cardinality");
        b.set(3, 4, false);
        check(!b.get(3) && b.get(2) && b.get(4), "set(from,to,false)");
        b.flip(2, 4); // flips 2,3
        check(!b.get(2) && b.get(3) && b.get(4), "flip(from,to)");
        b.clear(4, 5);
        check(!b.get(4) && b.get(3), "clear(from,to)");
    }

    static void testPreviousAndSentinel() {
        BitSet b = new BitSet();
        b.set(5);
        b.set(9);
        check(b.previousSetBit(8) == 5, "previousSetBit");
        check(b.previousSetBit(4) == -1, "previousSetBit -1 sentinel");
        check(b.previousSetBit(-1) == -1, "previousSetBit(-1) returns -1");
        check(b.previousClearBit(-1) == -1, "previousClearBit(-1) returns -1");
        check(b.previousClearBit(9) == 8, "previousClearBit");
        check(b.nextSetBit(10) == -1, "nextSetBit beyond -> -1");
        check(b.nextClearBit(0) == 0, "nextClearBit 0");
    }

    static void testValueOf() {
        expectThrows(NullPointerException.class, () -> BitSet.valueOf((long[]) null),
                     "valueOf((long[])null) NPE");
        expectThrows(NullPointerException.class, () -> BitSet.valueOf((byte[]) null),
                     "valueOf((byte[])null) NPE");
        expectThrows(NullPointerException.class, () -> BitSet.valueOf((LongBuffer) null),
                     "valueOf((LongBuffer)null) NPE");
        expectThrows(NullPointerException.class, () -> BitSet.valueOf((ByteBuffer) null),
                     "valueOf((ByteBuffer)null) NPE");

        long[] longs = new long[] { 0b1010L };
        BitSet b = BitSet.valueOf(longs);
        check(b.get(1) && b.get(3) && !b.get(0), "valueOf(long[]) bits");
        long[] round = b.toLongArray();
        check(java.util.Arrays.equals(longs, round), "toLongArray round-trip");

        byte[] bytes = new byte[] { 0b00001010 };
        BitSet c = BitSet.valueOf(bytes);
        check(c.get(1) && c.get(3), "valueOf(byte[]) bits");
        check(java.util.Arrays.equals(bytes, c.toByteArray()), "toByteArray round-trip");

        // trailing zero words are dropped.
        BitSet d = BitSet.valueOf(new long[] { 0L, 0L });
        check(d.toLongArray().length == 0, "trailing zero words dropped");
    }

    static void testIntersectsAndLogical() {
        BitSet a = new BitSet();
        a.set(0);
        a.set(2);
        BitSet b = new BitSet();
        b.set(2);
        check(a.intersects(b), "intersects true");
        BitSet c = new BitSet();
        c.set(1);
        check(!a.intersects(c), "intersects false");

        BitSet x = new BitSet();
        x.set(0);
        x.set(1);
        BitSet y = new BitSet();
        y.set(1);
        x.xor(y);
        check(x.get(0) && !x.get(1), "xor");
        x.andNot(y);
        check(x.get(0) && !x.get(1), "andNot (y has bit 1 cleared already)");
    }

    static void testCloneEqualsHashCodeToString() {
        BitSet a = new BitSet();
        a.set(3);
        a.set(100);
        BitSet b = (BitSet) a.clone();
        check(b != a, "clone distinct");
        check(b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        b.set(101);
        check(!a.get(101) && !b.equals(a), "clone structurally independent");
        check(!a.equals(null) && !a.equals("x"), "equals null/other-type false");
        check(a.toString() != null && a.toString().startsWith("{"), "toString braces");
        check(a.size() >= 101, "size >= word-aligned capacity");
    }

    static void testStream() {
        BitSet b = new BitSet();
        b.set(1);
        b.set(3);
        b.set(5);
        int[] got = b.stream().toArray();
        check(java.util.Arrays.equals(got, new int[] { 1, 3, 5 }), "stream set-bit indices in order");
    }

    static void testSerialization() throws Exception {
        BitSet original = new BitSet();
        original.set(0);
        original.set(63);
        original.set(64);
        original.set(1000);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BitSet revived = (BitSet) ois.readObject();
        ois.close();
        check(revived.equals(original), "Serializable round-trip equals");
        check(revived.cardinality() == 4, "Serializable round-trip cardinality");
    }

    public static void main(String[] args) throws Exception {
        testBasics();
        testClearFlip();
        testBitwise();
        testConstructorNegative();
        testIndexOutOfBounds();
        testRangeOperations();
        testPreviousAndSentinel();
        testValueOf();
        testIntersectsAndLogical();
        testCloneEqualsHashCodeToString();
        testStream();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
