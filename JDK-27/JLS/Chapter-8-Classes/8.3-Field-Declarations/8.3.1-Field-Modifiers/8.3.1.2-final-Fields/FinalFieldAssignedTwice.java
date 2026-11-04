/*
 * @test
 * @summary JLS 8.3.1.2 / 4.12.4: a final field may be assigned at most once; a
 *          final field with an initializer cannot be assigned again.
 * @compile/fail FinalFieldAssignedTwice.java
 */
class FinalFieldAssignedTwice {
    final int x = 1;
    FinalFieldAssignedTwice() { x = 2; }   // error: cannot assign a value to final variable x
}
