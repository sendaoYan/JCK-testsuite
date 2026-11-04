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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
