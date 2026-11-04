/*
 * @test
 * @summary JLS 10.6: each variable initializer must be assignment-compatible with
 *          the array's component type, or a compile-time error occurs.
 * @compile/fail ArrayInitializerIncompatibleType.java
 */

class ArrayInitializerIncompatibleType {
    void m() {
        int[] a = { 1, "two" };   // error: incompatible types: String cannot be converted to int
    }
}
