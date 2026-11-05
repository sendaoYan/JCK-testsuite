/*
 * @test
 * @summary java.lang.foreign.SegmentAllocator conformance: white-box structure + black-box behavior.
 * @run main SegmentAllocatorTest
 */
import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;

public class SegmentAllocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.SegmentAllocator");
        check(c.isInterface(), "interface");
        check(c.getMethod("allocate", long.class, long.class).getReturnType() == MemorySegment.class,
              "allocate(long,long) returns MemorySegment");
        check(c.getMethod("allocate", MemoryLayout.class).getReturnType() == MemorySegment.class,
              "allocate(MemoryLayout) returns MemorySegment");
        check(c.getMethod("allocate", long.class).getReturnType() == MemorySegment.class,
              "allocate(long) returns MemorySegment");
        check(c.getMethod("allocateFrom", ValueLayout.OfInt.class, int.class).getReturnType() == MemorySegment.class,
              "allocateFrom(OfInt,int) returns MemorySegment");
        check(c.getMethod("allocateFrom", String.class).getReturnType() == MemorySegment.class,
              "allocateFrom(String) returns MemorySegment");
        check(c.getMethod("slicingAllocator", MemorySegment.class).getReturnType() == SegmentAllocator.class,
              "slicingAllocator returns SegmentAllocator");
        check(c.getMethod("prefixAllocator", MemorySegment.class).getReturnType() == SegmentAllocator.class,
              "prefixAllocator returns SegmentAllocator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment pool = a.allocate(64, 8);

            SegmentAllocator slicing = SegmentAllocator.slicingAllocator(pool);
            MemorySegment s1 = slicing.allocate(ValueLayout.JAVA_INT);
            MemorySegment s2 = slicing.allocate(ValueLayout.JAVA_LONG);
            check(s1.byteSize() == 4, "slicing allocate(int layout) size");
            check(s2.byteSize() == 8, "slicing allocate(long layout) size");
            check(s2.address() > s1.address(), "slicing allocations advance");
            s1.set(ValueLayout.JAVA_INT, 0, 42);
            check(s1.get(ValueLayout.JAVA_INT, 0) == 42, "slicing segment writable");

            MemorySegment fromInt = slicing.allocateFrom(ValueLayout.JAVA_INT, 12345);
            check(fromInt.get(ValueLayout.JAVA_INT, 0) == 12345, "allocateFrom(OfInt,int)");
            MemorySegment fromLong = slicing.allocateFrom(ValueLayout.JAVA_LONG, 99L);
            check(fromLong.get(ValueLayout.JAVA_LONG, 0) == 99L, "allocateFrom(OfLong,long)");
            MemorySegment fromStr = slicing.allocateFrom("abc", StandardCharsets.UTF_8);
            check("abc".equals(fromStr.getString(0, StandardCharsets.UTF_8)), "allocateFrom(String)");
            MemorySegment fromArr = slicing.allocateFrom(ValueLayout.JAVA_INT, 1, 2, 3);
            check(fromArr.byteSize() == 12 && fromArr.getAtIndex(ValueLayout.JAVA_INT, 2) == 3,
                  "allocateFrom(int...)");

            SegmentAllocator prefix = SegmentAllocator.prefixAllocator(pool);
            MemorySegment p1 = prefix.allocate(ValueLayout.JAVA_INT);
            MemorySegment p2 = prefix.allocate(ValueLayout.JAVA_LONG);
            check(p1.byteSize() == 4 && p2.byteSize() == 8, "prefix allocation sizes");
            check(p1.address() == pool.address(), "prefix allocator starts at base");
            p1.set(ValueLayout.JAVA_INT, 0, 7);
            check(p1.get(ValueLayout.JAVA_INT, 0) == 7, "prefix segment writable");

            MemorySegment m1 = slicing.allocate(12);
            check(m1.byteSize() == 12, "allocate(long) size");
        }

        // Arena itself is a SegmentAllocator
        try (Arena a = Arena.ofConfined()) {
            SegmentAllocator alloc = a;
            MemorySegment seg = alloc.allocate(ValueLayout.JAVA_INT);
            check(seg.byteSize() == 4, "Arena as SegmentAllocator allocate(layout)");
        }
    }

    // ---- Part 3: deep contract (allocateFrom value matrix + NUL + derived allocators) ----
    static void testDeepContract() {
        try (Arena a = Arena.ofConfined()) {
            // varargs allocateFrom carries every value of the array
            MemorySegment ints = a.allocateFrom(ValueLayout.JAVA_INT, 10, 20, 30);
            check(ints.byteSize() == 12, "allocateFrom(OfInt, 1,2,3) size 12");
            check(ints.getAtIndex(ValueLayout.JAVA_INT, 0) == 10
                          && ints.getAtIndex(ValueLayout.JAVA_INT, 1) == 20
                          && ints.getAtIndex(ValueLayout.JAVA_INT, 2) == 30,
                  "allocateFrom(OfInt, ...) keeps all values");
            // empty varargs -> zero-size segment
            check(a.allocateFrom(ValueLayout.JAVA_INT).byteSize() == 0,
                  "allocateFrom(OfInt) with no values is empty");
            // boundary values survive
            MemorySegment extremes = a.allocateFrom(ValueLayout.JAVA_LONG,
                    Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE);
            check(extremes.getAtIndex(ValueLayout.JAVA_LONG, 0) == Long.MIN_VALUE
                          && extremes.getAtIndex(ValueLayout.JAVA_LONG, 3) == Long.MAX_VALUE,
                  "long extremes carried");
            MemorySegment floats = a.allocateFrom(ValueLayout.JAVA_FLOAT, 1.5f, -0.25f);
            check(floats.getAtIndex(ValueLayout.JAVA_FLOAT, 1) == -0.25f, "float values carried");

            // allocateFrom(String) appends a NUL terminator: byteSize == utf8 length + 1
            MemorySegment abc = a.allocateFrom("abc");
            check(abc.byteSize() == 4, "allocateFrom(\"abc\") is 4 bytes (NUL appended)");
            check(abc.get(ValueLayout.JAVA_BYTE, 3) == 0, "terminator byte is 0");
            // a Chinese char is 3 UTF-8 bytes + 1 terminator
            MemorySegment zh = a.allocateFrom("中");
            check(zh.byteSize() == 4, "allocateFrom(\"中\") is 3+1 bytes");
            // explicit charset variant
            MemorySegment ascii = a.allocateFrom("AB", StandardCharsets.US_ASCII);
            check(ascii.byteSize() == 3 && ascii.get(ValueLayout.JAVA_BYTE, 0) == 'A',
                  "allocateFrom(String, Charset) honors the charset");

            // slicing allocator hands out disjoint, in-bounds windows in call order
            MemorySegment pool = a.allocate(32, 8);
            SegmentAllocator slicer = SegmentAllocator.slicingAllocator(pool);
            MemorySegment w1 = slicer.allocate(8, 8);
            MemorySegment w2 = slicer.allocate(8, 8);
            MemorySegment w3 = slicer.allocate(8, 8);
            check(!w1.equals(w2) && !w2.equals(w3), "slices are distinct segments");
            check(w1.address() < w2.address() && w2.address() < w3.address(),
                  "slices advance monotonically");
            check(w3.address() + 8 <= pool.address() + 32, "slices stay in bounds");
            w1.set(ValueLayout.JAVA_LONG, 0, 1L);
            w2.set(ValueLayout.JAVA_LONG, 0, 2L);
            check(pool.get(ValueLayout.JAVA_LONG, 8) == 2L,
                  "slice writes land in the backing segment at their offset");

            // prefix allocator always returns the same window
            MemorySegment pre = a.allocate(8, 8);
            SegmentAllocator prefix = SegmentAllocator.prefixAllocator(pre);
            MemorySegment pa = prefix.allocateFrom(ValueLayout.JAVA_INT, 77);
            check(pa.address() == pre.address(), "prefix allocation starts at the base");
            check(pre.get(ValueLayout.JAVA_INT, 0) == 77, "prefix write is visible in the segment");
            MemorySegment pb = prefix.allocateFrom(ValueLayout.JAVA_INT, 88);
            check(pb.address() == pre.address(), "prefix allocations reuse the same window");
            check(pre.get(ValueLayout.JAVA_INT, 0) == 88, "later prefix write overwrites");

            // allocate(layout) alignment honors the layout's alignment
            MemorySegment align8 = a.allocate(MemoryLayout.structLayout(
                    ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
            check(align8.address() % 8 == 0, "allocate(struct) honors struct alignment");
            check(align8.byteSize() == 12, "allocate(struct) size equals layout size");
            // heap-backed allocateFrom: copy from an array segment into a fresh segment
            int[] data = {9, 8, 7};
            MemorySegment src = MemorySegment.ofArray(data);
            MemorySegment copy = a.allocate(12, 4);
            MemorySegment.copy(src, ValueLayout.JAVA_INT, 0, copy, ValueLayout.JAVA_INT, 0, 3);
            check(java.util.Arrays.equals(copy.toArray(ValueLayout.JAVA_INT), data),
                  "typed copy of a heap array segment round-trips");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
