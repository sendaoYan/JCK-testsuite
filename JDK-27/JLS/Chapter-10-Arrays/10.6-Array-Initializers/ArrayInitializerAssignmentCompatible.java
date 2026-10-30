/*
 * @test
 * @summary JLS 10.6: each variable initializer must be assignment-compatible with
 *          the array component type, or a compile-time error occurs.
 * @compile/fail ArrayInitializerAssignmentCompatible.java
 */

class ArrayInitializerAssignmentCompatible {
    void m() {
        int[] a = { 1, "x" };   // error: incompatible types
    }
}
