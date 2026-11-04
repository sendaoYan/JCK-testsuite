/*
 * @test
 * @summary JLS 10.6 (Java SE 8): each variable initializer must be
 *          assignment-compatible with the array's component type.
 * @compile/fail ArrayInitializerIncompatibleType.java
 */

class ArrayInitializerIncompatibleType {
    void m() {
        int[] a = { 1, "two" };   // error: incompatible types
    }
}
