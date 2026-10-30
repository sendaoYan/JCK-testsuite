/*
 * @test
 * @summary JLS 14.21: a yield statement transfers control to the enclosing switch
 *          expression, producing its value.
 * @run main YieldStatement
 */

public class YieldStatement {
    enum Day { MONDAY, TUESDAY }

    static int calculate(Day d) {
        return switch (d) {
            case MONDAY -> 1;
            default -> {
                int len = d.toString().length();
                yield len * len;
            }
        };
    }

    public static void main(String[] args) {
        if (calculate(Day.MONDAY) != 1) throw new AssertionError("arrow case");
        if (calculate(Day.TUESDAY) != 49) throw new AssertionError("yield case");  // 7 * 7
        System.out.println("PASS");
    }
}
