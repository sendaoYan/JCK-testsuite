/*
 * @test
 * @summary java.lang.Enum.EnumDesc conformance: of() and resolveConstantDesc.
 * @run main EnumDescTest
 */
import java.lang.Enum.EnumDesc;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

public class EnumDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        EnumDesc<TimeUnit> desc = EnumDesc.of(ClassDesc.of("java.util.concurrent", "TimeUnit"), "SECONDS");
        check(desc != null, "of non-null");
        check(desc.toString().contains("SECONDS"), "toString contains constant");
        check(desc.resolveConstantDesc(MethodHandles.lookup()) == TimeUnit.SECONDS, "resolveConstantDesc");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
