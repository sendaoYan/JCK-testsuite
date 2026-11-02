/*
 * @test
 * @summary java.lang.foreign.SequenceLayout conformance: interface and sequence layout transforms.
 * @run main SequenceLayoutTest
 */
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;

public class SequenceLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SequenceLayout.class.isInterface(), "is interface");
        SequenceLayout s = MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT);
        check(s.elementCount() == 4, "elementCount");
        check(s.elementLayout() == ValueLayout.JAVA_INT, "elementLayout");
        check(s.byteSize() == 16, "byteSize");
        check(s.withElementCount(6).elementCount() == 6, "withElementCount");
        SequenceLayout r = MemoryLayout.sequenceLayout(8, ValueLayout.JAVA_SHORT).reshape(2, 4);
        check(r.elementCount() == 2, "reshape outer");
        check(r.elementLayout() instanceof SequenceLayout, "reshape inner");
        check(r.flatten().elementCount() == 8, "flatten");
        check(s.withName("seq").name().get().equals("seq"), "withName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
