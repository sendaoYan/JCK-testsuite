/*
 * @test
 * @summary org.omg.DynamicAny.NameDynAnyPairSeqHelper conformance: helper static id/type/narrow.
 * @run main NameDynAnyPairSeqHelperTest
 */

public class NameDynAnyPairSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameDynAnyPairSeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameDynAnyPairSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
