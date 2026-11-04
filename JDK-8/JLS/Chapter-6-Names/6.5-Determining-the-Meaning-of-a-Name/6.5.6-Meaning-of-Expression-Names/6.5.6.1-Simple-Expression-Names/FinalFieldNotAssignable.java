/*
 * @test
 * @summary JLS 6.5.6.1 (Java SE 8): a final field is a value, not a variable, so
 *          it cannot be assigned to.
 * @compile/fail FinalFieldNotAssignable.java
 */

class FinalFieldNotAssignable {
    final int x = 1;
    void m() {
        x = 2;   // error: cannot assign a value to final variable x
    }
}
