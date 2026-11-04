/*
 * @test
 * @summary JLS 4.12.4 (Java SE 8): "It is a compile-time error if a final
 *          variable is assigned to unless it is definitely unassigned
 *          immediately prior to the assignment." Assigning a final variable a
 *          second time is a compile-time error.
 * @compile/fail FinalVariableAssignedTwice.java
 */

class FinalVariableAssignedTwice {
    void m() {
        final int x = 1;
        x = 2;   // error: cannot assign a value to final variable x
    }
}
