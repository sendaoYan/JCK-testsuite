/*
 * @test
 * @summary JLS 16.2.8: an assert statement does not change definite assignment; a
 *          variable definitely assigned before the assert is still definitely
 *          assigned after it.
 * @run main/othervm -ea AssertDA
 */

public class AssertDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x = 1;
        assert x == 1;          // condition evaluated (assertions enabled)
        check(x == 1, "x assigned before assert is still assigned after");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
