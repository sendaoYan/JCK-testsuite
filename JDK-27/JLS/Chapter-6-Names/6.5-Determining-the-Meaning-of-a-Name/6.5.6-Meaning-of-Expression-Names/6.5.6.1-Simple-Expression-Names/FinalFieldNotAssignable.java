/*
 * @test
 * @summary JLS 6.5.6.1 Example 6.5.6.1-1: a final field denotes a value, so it
 *          cannot appear as the left-hand side of an assignment.
 * @compile/fail FinalFieldNotAssignable.java
 */

class FinalFieldNotAssignable {
    static final int f = 3;
    void m() {
        f = 33;   // error: cannot assign a value to final variable f
    }
}
