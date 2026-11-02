/*
 * @test
 * @summary java.lang.foreign.UnionLayout conformance: interface and union layout operations.
 * @run main UnionLayoutTest
 */
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;

public class UnionLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(UnionLayout.class.isInterface(), "is interface");
        UnionLayout u = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(u.memberLayouts().size() == 2, "memberLayouts");
        check(u.byteSize() == 8, "byteSize max member");
        check(u.withName("u").name().get().equals("u"), "withName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
