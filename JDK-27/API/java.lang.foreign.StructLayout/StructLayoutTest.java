/*
 * @test
 * @summary java.lang.foreign.StructLayout conformance: interface and struct layout operations.
 * @run main StructLayoutTest
 */
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

public class StructLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(StructLayout.class.isInterface(), "is interface");
        StructLayout s = MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        check(s.memberLayouts().size() == 2, "memberLayouts");
        check(s.byteSize() == 8, "byteSize 2 ints");
        check(s.withName("st").name().get().equals("st"), "withName");
        check(s.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
