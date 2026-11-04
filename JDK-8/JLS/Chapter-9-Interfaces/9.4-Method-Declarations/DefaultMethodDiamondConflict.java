/*
 * @test
 * @summary JLS 9.4.1.3 (Java SE 8): when a class or interface inherits two default
 *          methods with override-equivalent signatures, the conflict must be
 *          resolved by an overriding declaration.
 * @compile/fail DefaultMethodDiamondConflict.java
 */

interface A { default void m() {} }
interface B { default void m() {} }

interface DefaultMethodDiamondConflict extends A, B {   // error: types A and B are incompatible
}
