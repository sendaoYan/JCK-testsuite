/*
 * @test
 * @summary JVMS 6.5: isub/imul, int MIN_VALUE / -1 overflow, and the long
 *          bitwise/shift instructions land/lor/lxor/lshr.
 * @compile IntArithMore.jasm
 * @run main IntArithMoreTest
 */

public class IntArithMoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(IntArithMore.isub(10, 3) == 7, "isub");
        check(IntArithMore.imul(6, 7) == 42, "imul");
        check(IntArithMore.idivMinOverflow() == Integer.MIN_VALUE, "MIN_VALUE / -1 wraps");

        check(IntArithMore.land(0xFFL, 0x0FL) == 0x0FL, "land");
        check(IntArithMore.lor(0xF0L, 0x0FL) == 0xFFL, "lor");
        check(IntArithMore.lxor(0xFFL, 0x0FL) == 0xF0L, "lxor");
        check(IntArithMore.lshr(-8L, 1) == -4L, "lshr arithmetic shift");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
