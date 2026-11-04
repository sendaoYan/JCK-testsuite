/*
 * @test
 * @summary JLS 16.9: a blank final instance field must be definitely assigned by
 *          the end of every constructor (or instance initializer); leaving it
 *          unassigned is a compile-time error.
 * @compile/fail BlankFinalFieldUnassignedFail.java
 */

class BlankFinalFieldUnassignedFail {
    final int f;

    BlankFinalFieldUnassignedFail() {
        // f is never assigned
    }
}
