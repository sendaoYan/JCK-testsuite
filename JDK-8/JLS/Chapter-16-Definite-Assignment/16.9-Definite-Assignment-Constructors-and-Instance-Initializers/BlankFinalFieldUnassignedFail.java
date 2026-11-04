/*
 * @test
 * @summary JLS 16.9 (Java SE 8): a blank final instance field must be definitely
 *          assigned by the end of every constructor; leaving it unassigned is a
 *          compile-time error.
 * @compile/fail BlankFinalFieldUnassignedFail.java
 */

class BlankFinalFieldUnassignedFail {
    final int f;

    BlankFinalFieldUnassignedFail() {
        // f is never assigned
    }
}
