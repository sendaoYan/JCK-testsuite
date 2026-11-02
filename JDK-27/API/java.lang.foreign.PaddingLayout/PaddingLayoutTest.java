/*
 * @test
 * @summary java.lang.foreign.PaddingLayout conformance: interface and paddingLayout operations.
 * @run main PaddingLayoutTest
 */
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.PaddingLayout;

public class PaddingLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(PaddingLayout.class.isInterface(), "is interface");
        PaddingLayout p = MemoryLayout.paddingLayout(8);
        check(p.byteSize() == 8, "byteSize");
        check(p.withName("pad").name().get().equals("pad"), "withName");
        check(p.withoutName().name().isEmpty(), "withoutName");
        check(p.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
