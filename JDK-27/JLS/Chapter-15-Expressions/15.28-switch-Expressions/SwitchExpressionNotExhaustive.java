/*
 * @test
 * @summary JLS 15.28: a switch expression must be exhaustive; a switch over an
 *          enum that omits some constants without a default is a compile-time
 *          error.
 * @compile/fail SwitchExpressionNotExhaustive.java
 */

class SwitchExpressionNotExhaustive {
    enum Day { MONDAY, TUESDAY }
    int m(Day d) {
        return switch (d) {
            case MONDAY -> 1;
        };   // error: the switch expression does not cover all possible input values
    }
}
