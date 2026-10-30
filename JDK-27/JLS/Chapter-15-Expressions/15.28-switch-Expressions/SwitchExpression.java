/*
 * @test
 * @summary JLS 15.28: a switch expression yields a value, using either arrow
 *          cases or yield statements, and must be exhaustive.
 * @run main SwitchExpression
 */

public class SwitchExpression {
    enum Day { MONDAY, TUESDAY }

    static int f(Day d) {
        return switch (d) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
        };
    }

    static int g(int x) {
        return switch (x) {
            case 0 -> { yield 100; }   // yield statement
            default -> x * 2;
        };
    }

    public static void main(String[] args) {
        if (f(Day.MONDAY) != 1) throw new AssertionError("f(MONDAY)");
        if (f(Day.TUESDAY) != 2) throw new AssertionError("f(TUESDAY)");
        if (g(0) != 100) throw new AssertionError("g(0) yield");
        if (g(5) != 10) throw new AssertionError("g(5)");

        System.out.println("PASS");
    }
}
