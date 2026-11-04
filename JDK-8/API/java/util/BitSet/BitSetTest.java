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

    // ============ contract deepening (appended) ============

    // Exact exception messages (stable across JDK 8 and 27; probed on both).
    static void testExceptionMessages() {
        try { new BitSet().set(-1); } catch (IndexOutOfBoundsException e) {
            check("bitIndex < 0: -1".equals(e.getMessage()), "set(-1) message");
        }
        try { new BitSet().set(5, 2); } catch (IndexOutOfBoundsException e) {
            check("fromIndex: 5 > toIndex: 2".equals(e.getMessage()), "set(from>to) message");
        }
        try { new BitSet().flip(5, 2); } catch (IndexOutOfBoundsException e) {
            check("fromIndex: 5 > toIndex: 2".equals(e.getMessage()), "flip(from>to) message");
        }
        try { new BitSet().nextSetBit(-1); } catch (IndexOutOfBoundsException e) {
            check("fromIndex < 0: -1".equals(e.getMessage()), "nextSetBit(-1) message");
        }
        try { new BitSet().previousSetBit(-2); } catch (IndexOutOfBoundsException e) {
            check("fromIndex < -1: -2".equals(e.getMessage()), "previousSetBit(-2) message");
        }
    }

    // null operands throw NPE for every logical/intersection operation.
    static void testNullNpe() {
        BitSet b = new BitSet();
        expectThrows(NullPointerException.class, () -> b.and(null), "and(null) NPE");
        expectThrows(NullPointerException.class, () -> b.or(null), "or(null) NPE");
        expectThrows(NullPointerException.class, () -> b.xor(null), "xor(null) NPE");
        expectThrows(NullPointerException.class, () -> b.andNot(null), "andNot(null) NPE");
        expectThrows(NullPointerException.class, () -> b.intersects(null), "intersects(null) NPE");
    }

    // size(): 64-bit word granularity; BitSet(0) is the degenerate zero-capacity set.
    static void testSize() {
        check(new BitSet().size() == 64, "default size 64");
        check(new BitSet(0).size() == 0, "BitSet(0) size 0");
        check(new BitSet(1).size() == 64 && new BitSet(63).size() == 64, "BitSet(<=63) size 64");
        check(new BitSet(64).size() == 64, "BitSet(64) size 64");
        check(new BitSet(65).size() == 128 && new BitSet(128).size() == 128, "BitSet(65/128) size 128");
        BitSet b = new BitSet();
        b.set(0);
        check(b.size() >= b.length() && b.size() % 64 == 0, "size word-aligned and >= length");
    }

    // toByteArray()/toLongArray() are little-endian and drop trailing zero units.
    static void testByteArrayExact() {
        BitSet b = new BitSet();
        b.set(0); b.set(3); b.set(8);
        byte[] bytes = b.toByteArray();
        check(bytes.length == 2 && (bytes[0] & 0xff) == 0b1001 && (bytes[1] & 0xff) == 0b1,
              "toByteArray little-endian: bits 0,3 -> 0x09; bit 8 -> next byte 0x01");
        BitSet one = new BitSet(); one.set(9);
        check(one.toByteArray().length == 2 && (one.toByteArray()[1] & 0xff) == 0b10,
              "bit 9 -> second byte 0x02");
        check(new BitSet().toByteArray().length == 0, "empty toByteArray length 0");

        BitSet w = new BitSet(); w.set(0); w.set(64);
        long[] longs = w.toLongArray();
        check(longs.length == 2 && longs[0] == 1L && longs[1] == 1L,
              "toLongArray little-endian words: bit 0 -> word0 bit0, bit 64 -> word1 bit0");
        BitSet t = new BitSet(); t.set(0); t.set(100);
        check(t.toLongArray().length == 2, "trailing zero words dropped beyond bit 100");
    }

    // valueOf over ByteBuffer slices: only the [position, limit) range is read, little-endian.
    static void testByteBufferValueOf() {
        byte[] data = { 1, 2, 3, 4 };
        // slice bytes 1..2 => values 2,3 => bits {1, 8, 9}
        BitSet s = BitSet.valueOf(java.nio.ByteBuffer.wrap(data, 1, 2));
        check(s.cardinality() == 3 && s.get(1) && s.get(8) && s.get(9),
              "ByteBuffer slice range honored");
        check(s.toByteArray().length == 2 && (s.toByteArray()[0] & 0xff) == 2
              && (s.toByteArray()[1] & 0xff) == 3, "ByteBuffer little-endian bytes");
        // LongBuffer variant equals the long[] variant
        long[] words = { 0b1011L, 0L };
        BitSet fromArr = BitSet.valueOf(words);
        BitSet fromBuf = BitSet.valueOf(java.nio.LongBuffer.wrap(new long[] { 0b1011L, 0L }));
        check(fromArr.equals(fromBuf), "valueOf(LongBuffer) equals valueOf(long[])");
        // empty buffers -> empty set
        check(BitSet.valueOf(java.nio.ByteBuffer.wrap(new byte[0])).isEmpty()
              && BitSet.valueOf(java.nio.LongBuffer.wrap(new long[0])).isEmpty(),
              "empty buffers produce empty BitSet");
    }

    // toString exact format.
    static void testToStringExact() {
        BitSet b = new BitSet();
        b.set(0); b.set(3); b.set(5);
        check("{0, 3, 5}".equals(b.toString()), "toString exact list");
        check("{}".equals(new BitSet().toString()), "empty toString {}");
        check("{7}".equals(BitSet.valueOf(new long[] { 1L << 7 }).toString()), "single-bit toString");
        BitSet big = new BitSet();
        big.set(1000);
        check("{1000}".equals(big.toString()), "high-bit toString");
    }

    // equals/hashCode: same content -> equal + same hash; documented empty hash 1234.
    static void testEqualsHashCodeDeep() {
        check(new BitSet().hashCode() == 1234, "empty hashCode is the documented 1234");
        BitSet a = new BitSet(); a.set(0); a.set(3); a.set(5);
        BitSet b = new BitSet(512); b.set(0); b.set(3); b.set(5); // different capacity
        check(a.equals(b) && b.equals(a), "equal content, different capacity still equal");
        check(a.hashCode() == b.hashCode(), "equal content same hashCode");
        check(a.hashCode() == 1275, "{0,3,5} hashCode == 1275 (stable)");
        // set-then-clear returns to the empty hash
        BitSet c = new BitSet(); c.set(42); c.clear(42);
        check(c.hashCode() == 1234 && c.isEmpty(), "cleared set returns to empty hash");
        check(!a.equals("not a bitset") && !a.equals(null), "equals non-BitSet/null false");
        // symmetry of inequality
        BitSet d = new BitSet(); d.set(0);
        check(!a.equals(d) && !d.equals(a), "inequality symmetric");
    }

    // Range edge cases: from==to is a legal empty range everywhere.
    static void testRangeEdgeCases() {
        BitSet b = new BitSet();
        b.set(1);
        check(b.get(2, 2).isEmpty(), "get(2,2) empty BitSet");
        b.flip(2, 2);
        check(b.cardinality() == 1, "flip(2,2) no-op");
        b.set(2, 2);
        check(b.cardinality() == 1, "set(2,2) no-op");
        b.clear(2, 2);
        check(b.cardinality() == 1, "clear(2,2) no-op");
        // range beyond current length
        BitSet e = new BitSet();
        e.set(0); e.set(3);
        check(e.get(5, 10).isEmpty(), "get beyond length all clear");
        // clear(to) is exclusive
        BitSet f = new BitSet();
        f.set(0, 5);
        f.clear(0, 4);
        check(!f.get(0) && !f.get(3) && f.get(4), "clear range excludes toIndex");
    }

    // Cross-word (64-bit boundary) behavior.
    static void testCrossWordBoundaries() {
        BitSet g = new BitSet();
        g.set(63); g.set(64);
        check(g.cardinality() == 2 && g.length() == 65, "bits 63+64 straddle word boundary");
        g.flip(63, 65);
        check(g.isEmpty(), "flip(63,65) clears both boundary bits");
        BitSet h = new BitSet();
        h.set(63); h.set(64); h.set(127);
        check(h.length() == 128 && h.cardinality() == 3, "bits 63,64,127");
        check(h.previousSetBit(126) == 64, "previousSetBit(126) crosses back over boundary");
        check(h.nextSetBit(65) == 127, "nextSetBit(65) jumps to 127");
        check(h.previousClearBit(127) == 126, "previousClearBit(127) is 126");
        // large scattered set: cardinality == stream count, length == highest+1
        BitSet big = new BitSet();
        for (int i = 0; i < 512; i += 7) big.set(i);
        check(big.cardinality() == 74 && big.stream().count() == 74,
              "cardinality equals stream count over 512 bits");
        check(big.length() == 512, "length == highest set bit + 1");
    }

    // Logical ops: the argument is never modified; `and` shrinks length; mixed lengths.
    static void testLogicalOperandUnmodified() {
        BitSet x = new BitSet(); x.set(0); x.set(100);
        BitSet y = new BitSet(); y.set(0);
        BitSet ySnapshot = (BitSet) y.clone();
        x.and(y);
        check(y.equals(ySnapshot), "and leaves argument unmodified");
        check(x.length() == 1 && x.cardinality() == 1,
              "and clears bits beyond argument's length (length shrinks)");

        BitSet o1 = new BitSet(); o1.set(5);
        BitSet o2 = new BitSet(); o2.set(200);
        BitSet o2Snapshot = (BitSet) o2.clone();
        o1.or(o2);
        check(o2.equals(o2Snapshot), "or leaves argument unmodified");
        check(o1.get(5) && o1.get(200) && o1.length() == 201, "or extends to longer operand");

        BitSet s1 = new BitSet(); s1.set(0); s1.set(1);
        BitSet s2 = new BitSet(); s2.set(1); s2.set(2);
        BitSet s2Snapshot = (BitSet) s2.clone();
        s1.xor(s2);
        check(s2.equals(s2Snapshot), "xor leaves argument unmodified");
        check(s1.get(0) && !s1.get(1) && s1.get(2), "xor symmetric difference");

        BitSet n1 = new BitSet(); n1.set(0); n1.set(1); n1.set(2);
        BitSet n2 = new BitSet(); n2.set(1);
        BitSet n2Snapshot = (BitSet) n2.clone();
        n1.andNot(n2);
        check(n2.equals(n2Snapshot), "andNot leaves argument unmodified");
        check(n1.get(0) && !n1.get(1) && n1.get(2), "andNot difference");

        // clear() removes every bit including far-apart ones
        BitSet far = new BitSet();
        far.set(0); far.set(200); far.set(5000);
        far.clear();
        check(far.isEmpty() && far.length() == 0 && far.cardinality() == 0, "clear() removes all bits");
    }

    // next/previous beyond length: documented sentinel behavior.
    static void testBeyondLengthProbes() {
        BitSet e = new BitSet();
        check(e.nextClearBit(100) == 100, "empty nextClearBit(100) == 100");
        check(e.previousClearBit(100) == 100, "empty previousClearBit(100) == 100");
        check(e.previousSetBit(100) == -1, "empty previousSetBit(100) == -1");
        BitSet w = new BitSet(); w.set(0); w.set(64);
        check(w.nextSetBit(w.length()) == -1, "nextSetBit(length) == -1");
        check(w.nextSetBit(65) == -1, "nextSetBit past last set == -1");
        check(w.previousClearBit(64) == 63, "previousClearBit inside set range");
    }

    // Serialization round-trip preserves sparse high bits across word boundaries.
    static void testSerializationDeep() throws Exception {
        BitSet original = new BitSet();
        original.set(0); original.set(63); original.set(64); original.set(511);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BitSet revived = (BitSet) ois.readObject();
        ois.close();
        check(revived.equals(original) && original.equals(revived), "deep round-trip equals");
        check(revived.cardinality() == 4 && revived.length() == 512,
              "deep round-trip keeps sparse high bits");
        check(revived.hashCode() == original.hashCode(), "round-trip hashCode preserved");
        // revived copy is mutable
        revived.clear(0);
        check(!revived.equals(original), "revived copy independently mutable");
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
        testExceptionMessages();
        testNullNpe();
        testSize();
        testByteArrayExact();
        testByteBufferValueOf();
        testToStringExact();
        testEqualsHashCodeDeep();
        testRangeEdgeCases();
        testCrossWordBoundaries();
        testLogicalOperandUnmodified();
        testBeyondLengthProbes();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
