/*
 * @test
 * @summary java.lang.foreign.MemoryLayout.PathElement conformance: interface and path element factories.
 * @run main PathElementTest
 */
import java.lang.foreign.MemoryLayout;

public class PathElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(MemoryLayout.PathElement.class.isInterface(), "is interface");
        check(MemoryLayout.PathElement.groupElement("x") != null, "groupElement name");
        check(MemoryLayout.PathElement.groupElement(0) != null, "groupElement long");
        check(MemoryLayout.PathElement.sequenceElement(1) != null, "sequenceElement long");
        check(MemoryLayout.PathElement.sequenceElement(1, 2) != null, "sequenceElement range");
        check(MemoryLayout.PathElement.sequenceElement() != null, "sequenceElement open");
        check(MemoryLayout.PathElement.dereferenceElement() != null, "dereferenceElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
