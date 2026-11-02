/*
 * @test
 * @summary java.lang.foreign.SegmentAllocator conformance: slicingAllocator/prefixAllocator and allocateFrom.
 * @run main SegmentAllocatorTest
 */
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public class SegmentAllocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SegmentAllocator.class.isInterface(), "is interface");
        SegmentAllocator sa = SegmentAllocator.slicingAllocator(MemorySegment.ofArray(new long[32]));
        check(sa.allocate(16, 8).byteSize() == 16, "allocate(16,8)");
        check(sa.allocateFrom(ValueLayout.JAVA_LONG, new long[]{1L,2L,3L}).byteSize() == 24, "allocateFrom long[]");
        check(sa.allocateFrom(ValueLayout.JAVA_INT, 42).get(ValueLayout.JAVA_INT, 0) == 42, "allocateFrom int");
        check(sa.allocateFrom("hello").byteSize() >= 5, "allocateFrom String");
        SegmentAllocator pa = SegmentAllocator.prefixAllocator(MemorySegment.ofArray(new byte[128]));
        check(pa.allocate(8).byteSize() == 8, "prefixAllocator allocate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
