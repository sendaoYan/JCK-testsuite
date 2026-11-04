/*
 * @test
 * @summary JLS 4.12.4 (Java SE 27): a blank final (declared without an
 *          initializer) may be assigned only once; assigning it a second time
 *          is a compile-time error because it is no longer definitely
 *          unassigned immediately prior to the second assignment.
 * @compile/fail BlankFinalAssignedTwice.java
 */

class BlankFinalAssignedTwice {
    void m() {
        final int x;
        x = 1;
        x = 2;   // error: variable x might already have been assigned
    }
}
