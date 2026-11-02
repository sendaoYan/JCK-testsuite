/*
 * @test
 * @summary java.lang.foreign.MemoryLayout conformance: interface and padding/sequence/struct/union factories.
 * @run main MemoryLayoutTest
 */
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;

public class MemoryLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(MemoryLayout.class.isInterface(), "is interface");
        MemoryLayout pad = MemoryLayout.paddingLayout(4);
        check(pad.byteSize() == 4 && pad instanceof PaddingLayout, "paddingLayout");
        MemoryLayout seq = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
        check(seq.byteSize() == 12 && seq instanceof SequenceLayout, "sequenceLayout");
        MemoryLayout str = MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        check(str instanceof StructLayout, "structLayout");
        MemoryLayout uni = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(uni instanceof UnionLayout, "unionLayout");
        check(str.byteAlignment() == 4, "byteAlignment");
        check(str.withName("s").name().get().equals("s"), "withName");
        check(str.withoutName().name().isEmpty(), "withoutName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
