/*
 * @test
 * @summary java.lang.foreign.MemoryLayout.PathElement conformance: white-box structure + black-box behavior.
 * @run main PathElementTest
 */
import java.lang.foreign.*;

public class PathElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.MemoryLayout$PathElement");
        check(c.isInterface(), "interface");
        check(c.getMethod("groupElement", String.class).getReturnType() == MemoryLayout.PathElement.class,
              "groupElement(String) returns PathElement");
        check(c.getMethod("groupElement", long.class).getReturnType() == MemoryLayout.PathElement.class,
              "groupElement(long) returns PathElement");
        check(c.getMethod("sequenceElement", long.class).getReturnType() == MemoryLayout.PathElement.class,
              "sequenceElement(long) returns PathElement");
        check(c.getMethod("sequenceElement", long.class, long.class).getReturnType() == MemoryLayout.PathElement.class,
              "sequenceElement(long,long) returns PathElement");
        check(c.getMethod("sequenceElement").getReturnType() == MemoryLayout.PathElement.class,
              "sequenceElement() returns PathElement");
        check(c.getMethod("dereferenceElement").getReturnType() == MemoryLayout.PathElement.class,
              "dereferenceElement returns PathElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(MemoryLayout.PathElement.groupElement("x") != null, "groupElement(String) non-null");
        check(MemoryLayout.PathElement.groupElement(0L) != null, "groupElement(long) non-null");
        check(MemoryLayout.PathElement.sequenceElement(1L) != null, "sequenceElement(long) non-null");
        check(MemoryLayout.PathElement.sequenceElement(1L, 2L) != null, "sequenceElement(long,long) non-null");
        check(MemoryLayout.PathElement.sequenceElement() != null, "sequenceElement() non-null");
        check(MemoryLayout.PathElement.dereferenceElement() != null, "dereferenceElement non-null");

        StructLayout st = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("i"), ValueLayout.JAVA_INT.withName("j"),
                ValueLayout.JAVA_LONG.withName("l"));
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("j")) == 4, "groupElement(String) offset");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement(1L)) == 4, "groupElement(long) offset");

        SequenceLayout seq = MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT);
        check(seq.byteOffset(MemoryLayout.PathElement.sequenceElement(2L)) == 8, "sequenceElement(long) offset");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
