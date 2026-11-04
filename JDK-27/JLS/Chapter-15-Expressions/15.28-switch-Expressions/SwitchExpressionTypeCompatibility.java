/*
 * @test
 * @summary JLS 15.28: a switch expression yields a single value whose type is
 *          common to all arms; arrow cases, multiple labels, and yield blocks
 *          are all forms of a switch arm.
 * @run main SwitchExpressionTypeCompatibility
 */

public class SwitchExpressionTypeCompatibility {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Day { MONDAY, TUESDAY, WEDNESDAY }

    static int f(Day d) {
        return switch (d) {
            case MONDAY, TUESDAY -> 1;
            case WEDNESDAY -> { yield 3; }
        };
    }

    static String g(Day d) {
        return switch (d) {
            case MONDAY -> "M";
            case TUESDAY -> "T";
            case WEDNESDAY -> "W";
        };
    }

    public static void main(String[] args) {
        check(f(Day.MONDAY) == 1, "arrow case");
        check(f(Day.TUESDAY) == 1, "multiple labels");
        check(f(Day.WEDNESDAY) == 3, "yield block");
        check(g(Day.MONDAY).equals("M"), "string result MONDAY");
        check(g(Day.TUESDAY).equals("T"), "string result TUESDAY");
        check(g(Day.WEDNESDAY).equals("W"), "string result WEDNESDAY");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
