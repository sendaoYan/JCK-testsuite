/*
 * @test
 * @summary JLS 4.12.4: "It is a compile-time error if a final variable is
 *          assigned to unless it is definitely unassigned immediately prior to
 *          the assignment." A final variable cannot be assigned a second time.
 * @compile/fail FinalVariableAssignedTwice.java
 */

class FinalVariableAssignedTwice {
    void m() {
        final int x = 1;
        x = 2;   // error: cannot assign a value to final variable x
    }
}
