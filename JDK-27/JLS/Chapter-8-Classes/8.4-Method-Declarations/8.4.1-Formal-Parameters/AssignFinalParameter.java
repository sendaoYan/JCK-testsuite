/*
 * @test
 * @summary JLS 8.4.1: it is a compile-time error if a formal parameter declared
 *          final is assigned to within the body of the method or constructor.
 * @compile/fail AssignFinalParameter.java
 */

class AssignFinalParameter {
    void m(final int x) {
        x = 5;   // error: cannot assign to final parameter
    }
}
