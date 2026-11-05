/*
 * @test
 * @summary java.lang.foreign.MemorySegment conformance: white-box structure + black-box behavior.
 * @run main MemorySegmentTest
 */
import java.lang.foreign.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MemorySegmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.MemorySegment");
        check(c.isInterface(), "interface");
        check(c.getField("NULL").getType() == MemorySegment.class, "NULL is a MemorySegment");
        check(c.getMethod("ofArray", byte[].class).getReturnType() == MemorySegment.class,
              "ofArray(byte[]) returns MemorySegment");
        check(c.getMethod("asSlice", long.class, long.class).getReturnType() == MemorySegment.class,
              "asSlice returns MemorySegment");
        check(c.getMethod("get", ValueLayout.OfInt.class, long.class).getReturnType() == int.class,
              "get(OfInt,long) returns int");
        check(c.getMethod("set", ValueLayout.OfInt.class, long.class, int.class).getReturnType() == void.class,
              "set(OfInt,long,int) returns void");
        check(c.getMethod("byteSize").getReturnType() == long.class, "byteSize returns long");
        check(c.getMethod("fill", byte.class).getReturnType() == MemorySegment.class, "fill returns MemorySegment");
        check(c.getMethod("copyFrom", MemorySegment.class).getReturnType() == MemorySegment.class,
              "copyFrom returns MemorySegment");
        check(c.getMethod("mismatch", MemorySegment.class).getReturnType() == long.class, "mismatch returns long");
        check(c.getMethod("asByteBuffer").getReturnType() == ByteBuffer.class, "asByteBuffer returns ByteBuffer");
        check(c.getMethod("toArray", ValueLayout.OfInt.class).getReturnType() == int[].class,
              "toArray(OfInt) returns int[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(MemorySegment.NULL.byteSize() == 0, "NULL segment has zero size");
        check(MemorySegment.NULL.address() == 0, "NULL segment has zero address");

        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(32, 8);
            check(seg.byteSize() == 32, "allocate byteSize");
            check(seg.isNative(), "native segment isNative");
            check(!seg.isMapped(), "arena segment not mapped");
            check(seg.scope().isAlive(), "segment scope alive");

            seg.fill((byte) 0x41);
            check(seg.get(ValueLayout.JAVA_BYTE, 0) == (byte) 0x41, "fill + get byte");
            seg.set(ValueLayout.JAVA_INT, 0, 42);
            check(seg.get(ValueLayout.JAVA_INT, 0) == 42, "set/get int");
            seg.setAtIndex(ValueLayout.JAVA_INT, 1, 7);
            check(seg.getAtIndex(ValueLayout.JAVA_INT, 1) == 7, "setAtIndex/getAtIndex int");
            seg.set(ValueLayout.JAVA_LONG, 8, 1234567890123L);
            check(seg.get(ValueLayout.JAVA_LONG, 8) == 1234567890123L, "set/get long");

            MemorySegment slice = seg.asSlice(4, 8);
            check(slice.byteSize() == 8, "asSlice size");
            check(slice.asSlice(2).byteSize() == 6, "asSlice(offset) size");

            seg.setString(0, "hello", StandardCharsets.US_ASCII);
            check("hello".equals(seg.getString(0, StandardCharsets.US_ASCII)), "setString/getString");
            check("hello".equals(seg.getString(0)), "getString default UTF-8");

            MemorySegment ro = seg.asReadOnly();
            check(ro.isReadOnly() && !seg.isReadOnly(), "asReadOnly marks read-only");
            check(ro.byteSize() == seg.byteSize(), "read-only keeps size");

            ByteBuffer bb = seg.asByteBuffer();
            check(bb.capacity() == 32, "asByteBuffer capacity");

            MemorySegment other = a.allocate(8, 8);
            MemorySegment.copy(seg, 0, other, 0, 4);
            check(other.get(ValueLayout.JAVA_INT, 0) == seg.get(ValueLayout.JAVA_INT, 0), "copy");
            check(seg.mismatch(other) >= 0, "mismatch of different content");
            MemorySegment same = a.allocate(8, 8);
            MemorySegment.copy(other, 0, same, 0, 8);
            check(same.mismatch(other) == -1, "mismatch identical content is -1");
            other.copyFrom(same);
            check(other.mismatch(same) == -1, "copyFrom then equal");
            check(seg.asOverlappingSlice(slice).isPresent(), "asOverlappingSlice finds overlap");
            check(seg.asOverlappingSlice(a.allocate(4, 4)).isEmpty(), "asOverlappingSlice no overlap");
        }

        int[] arr = {1, 2, 3, 4};
        MemorySegment as = MemorySegment.ofArray(arr);
        check(!as.isNative(), "ofArray segment not native");
        check(as.heapBase().isPresent(), "ofArray has heapBase");
        check(as.byteSize() == 16, "ofArray(int[4]) byteSize 16");
        check(java.util.Arrays.equals(as.toArray(ValueLayout.JAVA_INT), arr), "toArray roundtrip");
        check(as.getAtIndex(ValueLayout.JAVA_INT, 2) == 3, "getAtIndex on ofArray");

        byte[] bytes = {10, 20, 30};
        MemorySegment bs = MemorySegment.ofArray(bytes);
        check(java.util.Arrays.equals(bs.toArray(ValueLayout.JAVA_BYTE), bytes), "toArray(byte) roundtrip");
    }

    // ---- Part 3: deep contract (value table, bounds/alignment/read-only exception matrix) ----
    static void testValueTable() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(64, 8);
            // int boundary values
            int[] ints = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};
            for (int i = 0; i < ints.length; i++) {
                seg.set(ValueLayout.JAVA_INT, i * 4L, ints[i]);
                check(seg.get(ValueLayout.JAVA_INT, i * 4L) == ints[i],
                      "int value " + ints[i] + " round-trips");
            }
            // long boundary values
            long[] longs = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};
            for (int i = 0; i < longs.length; i++) {
                seg.set(ValueLayout.JAVA_LONG, i * 8L, longs[i]);
                check(seg.get(ValueLayout.JAVA_LONG, i * 8L) == longs[i],
                      "long value " + longs[i] + " round-trips");
            }
            // byte/short/char extremes
            seg.set(ValueLayout.JAVA_BYTE, 0, Byte.MAX_VALUE);
            check(seg.get(ValueLayout.JAVA_BYTE, 0) == Byte.MAX_VALUE, "byte max");
            seg.set(ValueLayout.JAVA_SHORT, 0, Short.MIN_VALUE);
            check(seg.get(ValueLayout.JAVA_SHORT, 0) == Short.MIN_VALUE, "short min");
            seg.set(ValueLayout.JAVA_CHAR, 0, Character.MAX_VALUE);
            check(seg.get(ValueLayout.JAVA_CHAR, 0) == Character.MAX_VALUE, "char max");
            seg.set(ValueLayout.JAVA_BOOLEAN, 0, true);
            check(seg.get(ValueLayout.JAVA_BOOLEAN, 0), "boolean true round-trips");
            // float/double keep exact bits
            seg.set(ValueLayout.JAVA_FLOAT, 0, Float.MIN_VALUE);
            check(seg.get(ValueLayout.JAVA_FLOAT, 0) == Float.MIN_VALUE, "float min round-trips");
            seg.set(ValueLayout.JAVA_DOUBLE, 0, Double.NaN);
            check(Double.isNaN(seg.get(ValueLayout.JAVA_DOUBLE, 0)), "double NaN round-trips");
            // getAtIndex addressing: index i accesses offset i*byteSize
            seg.setAtIndex(ValueLayout.JAVA_INT, 3, 99);
            check(seg.get(ValueLayout.JAVA_INT, 12) == 99,
                  "setAtIndex(3) writes at byte offset 12 (index addressing)");
            check(seg.getAtIndex(ValueLayout.JAVA_INT, 3) == 99, "getAtIndex(3) reads it back");
        }
    }

    static void testBoundsMatrix() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);   // exactly 8 bytes
            // 4-byte read at offset 5 runs past the end -> IndexOutOfBoundsException
            try { seg.get(ValueLayout.JAVA_INT, 5); check(false, "overrunning read rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "get INT@5 on 8B -> IOOBE"); }
            try { seg.set(ValueLayout.JAVA_INT, 5, 1); check(false, "overrunning write rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "set INT@5 on 8B -> IOOBE"); }
            try { seg.get(ValueLayout.JAVA_BYTE, -1); check(false, "negative offset rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "get BYTE@-1 -> IOOBE"); }
            try { seg.getAtIndex(ValueLayout.JAVA_INT, -1); check(false, "negative index rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "getAtIndex(-1) -> IOOBE"); }
            try { seg.setAtIndex(ValueLayout.JAVA_INT, 2, 1); check(false, "index past end rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "setAtIndex(2) on 8B -> IOOBE"); }
            try { seg.asSlice(9); check(false, "slice past end rejected"); }
            catch (IndexOutOfBoundsException expected) { check(true, "asSlice(9) on 8B -> IOOBE"); }
        }
    }

    static void testAlignmentMatrix() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(32, 8);
            // JDK-27 still enforces layout alignment on access (measured):
            // JAVA_LONG (align 8) at offset 4 -> IllegalArgumentException
            try { seg.get(ValueLayout.JAVA_LONG, 4); check(false, "misaligned read rejected"); }
            catch (IllegalArgumentException expected) { check(true, "get LONG@4 -> IAE (misaligned)"); }
            try { seg.set(ValueLayout.JAVA_LONG, 4, 1L); check(false, "misaligned write rejected"); }
            catch (IllegalArgumentException expected) { check(true, "set LONG@4 -> IAE (misaligned)"); }
            // the UNALIGNED variant is legal at offset 4
            seg.set(ValueLayout.JAVA_LONG_UNALIGNED, 4, 1L);
            check(seg.get(ValueLayout.JAVA_LONG_UNALIGNED, 4) == 1L,
                  "JAVA_LONG_UNALIGNED@4 succeeds");
            // heap segments have 1-byte base alignment, so JAVA_INT@1 is misaligned there
            MemorySegment heap = MemorySegment.ofArray(new byte[8]);
            try { heap.get(ValueLayout.JAVA_INT, 1); check(false, "heap misaligned read rejected"); }
            catch (IllegalArgumentException expected) { check(true, "heap get INT@1 -> IAE"); }
            heap.set(ValueLayout.JAVA_INT_UNALIGNED, 1, 0x41424344);
            check(heap.get(ValueLayout.JAVA_INT_UNALIGNED, 1) == 0x41424344,
                  "heap INT_UNALIGNED@1 succeeds and round-trips");
        }
    }

    static void testReadOnlyMatrix() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment rw = a.allocate(8, 8);
            rw.set(ValueLayout.JAVA_LONG, 0, 7L);
            MemorySegment ro = rw.asReadOnly();
            // JDK-27 measured: read-only violations throw IllegalArgumentException
            try { ro.set(ValueLayout.JAVA_LONG, 0, 1L); check(false, "ro write rejected"); }
            catch (IllegalArgumentException expected) { check(true, "ro set -> IllegalArgumentException"); }
            try { ro.fill((byte) 1); check(false, "ro fill rejected"); }
            catch (IllegalArgumentException expected) { check(true, "ro fill -> IllegalArgumentException"); }
            try { ro.copyFrom(a.allocate(8, 8)); check(false, "ro copyFrom rejected"); }
            catch (IllegalArgumentException expected) { check(true, "ro copyFrom -> IllegalArgumentException"); }
            try { ro.setString(0, "x"); check(false, "ro setString rejected"); }
            catch (IllegalArgumentException expected) { check(true, "ro setString -> IllegalArgumentException"); }
            check(ro.get(ValueLayout.JAVA_LONG, 0) == 7L, "reading through a read-only view works");
            check(ro.asReadOnly() == ro || ro.asReadOnly().equals(ro),
                  "asReadOnly is idempotent");
            // heap read-only view behaves the same
            MemorySegment hro = MemorySegment.ofArray(new int[]{5}).asReadOnly();
            try { hro.setAtIndex(ValueLayout.JAVA_INT, 0, 9); check(false, "heap ro write rejected"); }
            catch (IllegalArgumentException expected) { check(true, "heap ro setAtIndex -> IAE"); }
        }
    }

    static void testEqualityAndViews() {
        try (Arena a = Arena.ofConfined()) {
            // equals is base+offset+size identity, NOT content equality (measured)
            MemorySegment s1 = a.allocate(8, 8);
            MemorySegment s2 = a.allocate(8, 8);
            s1.fill((byte) 0);
            s2.fill((byte) 0);
            check(!s1.equals(s2), "equal content at different addresses is NOT equals");
            check(s1.asSlice(0).equals(s1), "asSlice(0) view equals its parent");
            check(s1.asSlice(0).hashCode() == s1.hashCode(), "view hashCode agrees");
            check(s1.asSlice(2, 4).byteSize() == 4 && !s1.asSlice(2, 4).equals(s1),
                  "different window is not equals");
            // parent/child write-through (offset 4 keeps the slice 4-byte aligned)
            MemorySegment parent = a.allocate(16, 8);
            MemorySegment child = parent.asSlice(4, 4);
            child.set(ValueLayout.JAVA_INT, 0, 0x2A2A2A2A);
            check(parent.get(ValueLayout.JAVA_INT, 4) == 0x2A2A2A2A,
                  "slice write is visible through the parent");
            // copy leaves bytes outside the copied window untouched
            MemorySegment src = a.allocate(8, 8);
            src.fill((byte) 0x11);
            MemorySegment dst = a.allocate(8, 8);
            dst.fill((byte) 0x22);
            MemorySegment.copy(src, 1, dst, 1, 3);
            check(dst.get(ValueLayout.JAVA_BYTE, 0) == 0x22
                          && dst.get(ValueLayout.JAVA_BYTE, 4) == 0x22,
                  "copy window preserves bytes outside [1,4)");
            check(dst.get(ValueLayout.JAVA_BYTE, 1) == 0x11
                          && dst.get(ValueLayout.JAVA_BYTE, 3) == 0x11,
                  "copy window copies the interior bytes");
            // typed copy round trip
            MemorySegment t1 = a.allocate(8, 8);
            t1.set(ValueLayout.JAVA_LONG, 0, 0x0706050403020100L);
            MemorySegment t2 = a.allocate(8, 8);
            MemorySegment.copy(t1, ValueLayout.JAVA_LONG, 0, t2, ValueLayout.JAVA_LONG, 0, 1);
            check(t2.get(ValueLayout.JAVA_LONG, 0) == 0x0706050403020100L, "typed copy round-trip");
        }
    }

    static void testStringsAndNull() {
        try (Arena a = Arena.ofConfined()) {
            // UTF-8: "中" is one char but three bytes; NUL terminator appended
            MemorySegment s = a.allocateFrom("中");
            check(s.byteSize() == 4, "allocateFrom(\"中\") = 3 UTF-8 bytes + NUL");
            check(s.get(ValueLayout.JAVA_BYTE, 3) == 0, "NUL terminator materialized");
            check("中".equals(s.getString(0)), "getString reads the Chinese char back");
            // empty string is just the terminator
            MemorySegment e = a.allocateFrom("");
            check(e.byteSize() == 1 && e.get(ValueLayout.JAVA_BYTE, 0) == 0,
                  "allocateFrom(\"\") is one NUL byte");
            check("".equals(e.getString(0)), "empty string round-trips");
            // setString/getString with embedded content and explicit charset
            MemorySegment u = a.allocate(16, 8);
            u.setString(0, "ab", StandardCharsets.US_ASCII);
            check("ab".equals(u.getString(0, StandardCharsets.US_ASCII)),
                  "explicit charset round-trip");
            // getString with no terminator in range -> IndexOutOfBoundsException (measured)
            MemorySegment nt = a.allocate(4, 1);
            nt.fill((byte) 'A');
            try { nt.getString(0); check(false, "unterminated string rejected"); }
            catch (IndexOutOfBoundsException expected) {
                check(true, "getString without NUL -> IOOBE"); }
        }

        // MemorySegment.NULL contract (JDK-27 measured)
        check(MemorySegment.NULL.address() == 0, "NULL address is 0");
        check(MemorySegment.NULL.byteSize() == 0, "NULL size is 0");
        check(MemorySegment.NULL.isNative(), "NULL is a native segment");
        check(MemorySegment.NULL.scope().isAlive(), "NULL scope (global) is always alive");
        check(MemorySegment.NULL.isAccessibleBy(Thread.currentThread()),
              "NULL is accessible from any thread");
        try { MemorySegment.NULL.get(ValueLayout.JAVA_BYTE, 0); check(false, "NULL read rejected"); }
        catch (IndexOutOfBoundsException expected) { check(true, "NULL get -> IOOBE"); }
        check(MemorySegment.ofAddress(0).equals(MemorySegment.NULL),
              "ofAddress(0) equals NULL");
        check(!MemorySegment.NULL.equals(MemorySegment.ofAddress(1)),
              "NULL != ofAddress(1)");
    }

    static void testHeapAndElements() {
        // asByteBuffer only exists for byte[]-backed heap segments (measured)
        MemorySegment ints = MemorySegment.ofArray(new int[]{1, 2, 3, 4});
        try { ints.asByteBuffer(); check(false, "int[] asByteBuffer rejected"); }
        catch (UnsupportedOperationException expected) {
            check(true, "asByteBuffer on int[] -> UnsupportedOperationException"); }
        MemorySegment bytes = MemorySegment.ofArray(new byte[]{1, 2, 3, 4});
        check(bytes.asByteBuffer().capacity() == 4, "byte[] asByteBuffer capacity");

        // elements/spliterator count follows the element layout given
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(16, 8);
            check(seg.elements(ValueLayout.JAVA_INT).count() == 4,
                  "elements(JAVA_INT) yields 4 elements on 16B");
            check(seg.spliterator(ValueLayout.JAVA_INT).estimateSize() == 4,
                  "spliterator(JAVA_INT) estimates 4");
            // spliterator iterates non-overlapping 4-byte windows
            java.util.List<MemorySegment> parts = seg.elements(ValueLayout.JAVA_INT).toList();
            check(parts.size() == 4 && parts.get(3).address() - parts.get(0).address() == 12,
                  "element windows advance by element size");
        }
    }

    static void testDeepContract() {
        testValueTable();
        testBoundsMatrix();
        testAlignmentMatrix();
        testReadOnlyMatrix();
        testEqualityAndViews();
        testStringsAndNull();
        testHeapAndElements();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
