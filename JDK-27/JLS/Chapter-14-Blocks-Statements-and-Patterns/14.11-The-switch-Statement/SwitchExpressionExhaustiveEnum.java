/*
 * @test
 * @summary JLS 14.11.1.1: a switch expression over an enum is exhaustive when
 *          every constant has a matching case, so no default is required and the
 *          expression yields the selected value.
 * @run main SwitchExpressionExhaustiveEnum
 */

enum Day { MONDAY, TUESDAY, WEDNESDAY }

public class SwitchExpressionExhaustiveEnum {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static int rank(Day d) {
        return switch (d) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
        };
    }

    public static void main(String[] args) {
        check(rank(Day.MONDAY) == 1, "MONDAY");
        check(rank(Day.TUESDAY) == 2, "TUESDAY");
        check(rank(Day.WEDNESDAY) == 3, "WEDNESDAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
