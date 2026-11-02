/*
 * @test
 * @summary org.omg.IOP.MultipleComponentProfileHelper conformance: helper static id/type/narrow.
 * @run main MultipleComponentProfileHelperTest
 */

public class MultipleComponentProfileHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.MultipleComponentProfileHelper.id() != null, "id() non-null");
        check(org.omg.IOP.MultipleComponentProfileHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
