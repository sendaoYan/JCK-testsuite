/*
 * @test
 * @summary JLS 15.26.1 (with 5.2): a simple assignment must undergo assignment
 *          conversion; assigning an out-of-range int constant to a byte is a
 *          compile-time error.
 * @compile/fail IncompatibleAssignment.java
 */

class IncompatibleAssignment {
    void m() {
        byte b = 200;   // error: incompatible types: possible lossy conversion from int to byte
    }
}
