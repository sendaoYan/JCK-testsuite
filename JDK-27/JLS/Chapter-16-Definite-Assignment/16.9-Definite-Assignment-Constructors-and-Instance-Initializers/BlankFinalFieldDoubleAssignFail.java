/*
 * @test
 * @summary JLS 16.9: a blank final instance field may not be assigned a second
 *          time in a constructor.
 * @compile/fail BlankFinalFieldDoubleAssignFail.java
 */

class BlankFinalFieldDoubleAssignFail {
    final int f;

    BlankFinalFieldDoubleAssignFail() {
        f = 1;
        f = 2;   // error: variable f might already have been assigned
    }
}
