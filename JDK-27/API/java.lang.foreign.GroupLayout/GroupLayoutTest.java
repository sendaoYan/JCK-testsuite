/*
 * @test
 * @summary java.lang.foreign.GroupLayout conformance: interface and struct/union memberLayouts.
 * @run main GroupLayoutTest
 */
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

public class GroupLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(GroupLayout.class.isInterface(), "is interface");
        GroupLayout g = MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        check(g.memberLayouts().size() == 2, "struct members");
        check(g.byteSize() == 8, "struct byteSize");
        GroupLayout u = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(u.memberLayouts().size() == 2, "union members");
        check(u.byteSize() == 8, "union byteSize");
        check(g.withName("g").name().get().equals("g"), "withName");
        check(g.withoutName().name().isEmpty(), "withoutName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
