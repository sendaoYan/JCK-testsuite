/*
 * @test
 * @summary java.lang.foreign.MemorySegment conformance: ofArray/get/set/asSlice/mismatch/toArray/scope.
 * @run main MemorySegmentTest
 */
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class MemorySegmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(MemorySegment.class.isInterface(), "is interface");
        MemorySegment seg = MemorySegment.ofArray(new int[]{10, 20, 30});
        check(seg.byteSize() == 12, "byteSize");
        check(!seg.isReadOnly(), "not read-only");
        check(seg.get(ValueLayout.JAVA_INT, 0) == 10, "get offset 0");
        check(seg.get(ValueLayout.JAVA_INT, 8) == 30, "get offset 8");
        seg.set(ValueLayout.JAVA_INT, 4, 99);
        check(seg.get(ValueLayout.JAVA_INT, 4) == 99, "set/get roundtrip");
        check(seg.getAtIndex(ValueLayout.JAVA_INT, 1) == 99, "getAtIndex");
        seg.setAtIndex(ValueLayout.JAVA_INT, 1, 55);
        check(seg.getAtIndex(ValueLayout.JAVA_INT, 1) == 55, "setAtIndex");
        check(seg.asSlice(4, 8).byteSize() == 8, "asSlice");
        check(seg.asReadOnly().isReadOnly(), "asReadOnly");
        check(seg.mismatch(seg) == -1, "mismatch self == -1");
        check(MemorySegment.ofArray(new byte[]{1,2,3,4}).toArray(ValueLayout.JAVA_BYTE).length == 4, "toArray");
        check(seg.scope().isAlive(), "scope alive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
