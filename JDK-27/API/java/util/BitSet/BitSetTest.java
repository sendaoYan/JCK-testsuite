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

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
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

        // ---- boundary/equivalence additions (M2) ----
        BitSet empty = new BitSet();
        check(empty.isEmpty() && empty.length() == 0 && empty.cardinality() == 0,
              "empty isEmpty/length/cardinality");
        check(empty.nextSetBit(0) == -1 && empty.nextClearBit(0) == 0,
              "empty nextSetBit -1 / nextClearBit 0");
        check(empty.previousSetBit(0) == -1 && empty.previousClearBit(0) == 0,
              "empty previousSetBit/previousClearBit");
        check(BitSet.valueOf(new long[0]).isEmpty(), "valueOf(empty long[]) empty");
        check(BitSet.valueOf(new byte[0]).isEmpty(), "valueOf(empty byte[]) empty");
        check(empty.toByteArray().length == 0, "empty toByteArray zero length");
        check(empty.toLongArray().length == 0, "empty toLongArray zero length");

        expectThrows(() -> { new BitSet().set(-1); }, IndexOutOfBoundsException.class, "set(-1) IOOBE");
        expectThrows(() -> { new BitSet().get(-1); }, IndexOutOfBoundsException.class, "get(-1) IOOBE");
        expectThrows(() -> { new BitSet().clear(-1); }, IndexOutOfBoundsException.class, "clear(-1) IOOBE");
        expectThrows(() -> { new BitSet().flip(-1); }, IndexOutOfBoundsException.class, "flip(-1) IOOBE");
        expectThrows(() -> { new BitSet().get(3, 2); }, IndexOutOfBoundsException.class, "get(from>to) IOOBE");
        expectThrows(() -> { new BitSet().set(3, 2); }, IndexOutOfBoundsException.class, "set(from>to) IOOBE");
        expectThrows(() -> { new BitSet().nextSetBit(-1); }, IndexOutOfBoundsException.class, "nextSetBit(-1) IOOBE");
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

    // ---- 契约深化（追加） ----

    // 精确异常消息（JDK-8 与 JDK-27 实测一致）。
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

    // 逻辑/交集运算的 null 参数一律 NPE。
    static void testNullNpe() {
        BitSet b = new BitSet();
        expectThrows(() -> b.and(null), NullPointerException.class, "and(null) NPE");
        expectThrows(() -> b.or(null), NullPointerException.class, "or(null) NPE");
        expectThrows(() -> b.xor(null), NullPointerException.class, "xor(null) NPE");
        expectThrows(() -> b.andNot(null), NullPointerException.class, "andNot(null) NPE");
        expectThrows(() -> b.intersects(null), NullPointerException.class, "intersects(null) NPE");
    }

    // size()：64 位字粒度；BitSet(0) 是零容量的退化形态。
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

    // toByteArray()/toLongArray()：小端序且丢弃末尾零单元。
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

    // valueOf(ByteBuffer) 只读 [position, limit) 范围，小端序。
    static void testByteBufferValueOf() {
        byte[] data = { 1, 2, 3, 4 };
        // slice bytes 1..2 => values 2,3 => bits {1, 8, 9}
        BitSet s = BitSet.valueOf(java.nio.ByteBuffer.wrap(data, 1, 2));
        check(s.cardinality() == 3 && s.get(1) && s.get(8) && s.get(9),
              "ByteBuffer slice range honored");
        check(s.toByteArray().length == 2 && (s.toByteArray()[0] & 0xff) == 2
              && (s.toByteArray()[1] & 0xff) == 3, "ByteBuffer little-endian bytes");
        BitSet fromArr = BitSet.valueOf(new long[] { 0b1011L, 0L });
        BitSet fromBuf = BitSet.valueOf(java.nio.LongBuffer.wrap(new long[] { 0b1011L, 0L }));
        check(fromArr.equals(fromBuf), "valueOf(LongBuffer) equals valueOf(long[])");
        check(BitSet.valueOf(java.nio.ByteBuffer.wrap(new byte[0])).isEmpty()
              && BitSet.valueOf(java.nio.LongBuffer.wrap(new long[0])).isEmpty(),
              "empty buffers produce empty BitSet");
    }

    // toString 精确格式。
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

    // equals/hashCode：同内容相等且同 hash；文档化的空集 hash 1234。
    static void testEqualsHashCodeDeep() {
        check(new BitSet().hashCode() == 1234, "empty hashCode is the documented 1234");
        BitSet a = new BitSet(); a.set(0); a.set(3); a.set(5);
        BitSet b = new BitSet(512); b.set(0); b.set(3); b.set(5); // 不同容量
        check(a.equals(b) && b.equals(a), "equal content, different capacity still equal");
        check(a.hashCode() == b.hashCode(), "equal content same hashCode");
        check(a.hashCode() == 1275, "{0,3,5} hashCode == 1275 (stable)");
        BitSet c = new BitSet(); c.set(42); c.clear(42);
        check(c.hashCode() == 1234 && c.isEmpty(), "cleared set returns to empty hash");
        check(!a.equals("not a bitset") && !a.equals(null), "equals non-BitSet/null false");
        BitSet d = new BitSet(); d.set(0);
        check(!a.equals(d) && !d.equals(a), "inequality symmetric");
    }

    // 范围边界：from==to 是合法空区间（对全部四方法）。
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
        BitSet e = new BitSet();
        e.set(0); e.set(3);
        check(e.get(5, 10).isEmpty(), "get beyond length all clear");
        BitSet f = new BitSet();
        f.set(0, 5);
        f.clear(0, 4);
        check(!f.get(0) && !f.get(3) && f.get(4), "clear range excludes toIndex");
    }

    // 跨 64 位字边界行为。
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
        BitSet big = new BitSet();
        for (int i = 0; i < 512; i += 7) big.set(i);
        check(big.cardinality() == 74 && big.stream().count() == 74,
              "cardinality equals stream count over 512 bits");
        check(big.length() == 512, "length == highest set bit + 1");
    }

    // 逻辑运算不修改参数；and 收缩 length；混合长度。
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

        BitSet far = new BitSet();
        far.set(0); far.set(200); far.set(5000);
        far.clear();
        check(far.isEmpty() && far.length() == 0 && far.cardinality() == 0, "clear() removes all bits");
    }

    // 超出 length 的探针：哨兵行为。
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

    // 序列化往返保留跨字边界的稀疏高位。
    static void testSerializationDeep() throws Exception {
        BitSet original = new BitSet();
        original.set(0); original.set(63); original.set(64); original.set(511);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        BitSet revived = (BitSet) ois.readObject();
        ois.close();
        check(revived.equals(original) && original.equals(revived), "deep round-trip equals");
        check(revived.cardinality() == 4 && revived.length() == 512,
              "deep round-trip keeps sparse high bits");
        check(revived.hashCode() == original.hashCode(), "round-trip hashCode preserved");
        revived.clear(0);
        check(!revived.equals(original), "revived copy independently mutable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
