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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
