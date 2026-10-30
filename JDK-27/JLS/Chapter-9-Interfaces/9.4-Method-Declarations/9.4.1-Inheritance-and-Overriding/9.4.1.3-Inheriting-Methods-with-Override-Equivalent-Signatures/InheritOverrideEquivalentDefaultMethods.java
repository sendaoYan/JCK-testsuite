/*
 * @test
 * @summary JLS 9.4.1.3: it is a compile-time error if an interface inherits a
 *          default method whose signature is override-equivalent with another
 *          inherited method.
 * @compile/fail InheritOverrideEquivalentDefaultMethods.java
 */

interface A { default void m() {} }
interface B { default void m() {} }
interface InheritOverrideEquivalentDefaultMethods extends A, B {}   // error: two default m()
