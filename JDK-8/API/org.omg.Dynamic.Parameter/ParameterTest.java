/*
 * @test
 * @summary org.omg.Dynamic.Parameter conformance: value type fields and constructors.
 * @run main ParameterTest
 */

public class ParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.Dynamic.Parameter s0 = new org.omg.Dynamic.Parameter();
        check(s0.argument == null, "no-arg argument default");
        check(s0.mode == null, "no-arg mode default");
        org.omg.Dynamic.Parameter s1 = new org.omg.Dynamic.Parameter(null, null);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
