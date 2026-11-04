/*
 * @test
 * @summary JLS 4.4 (Java SE 8): "It is a compile-time error if any of the types
 *          I1 ... In [additional bounds] is a class type or type variable."
 * @compile/fail ClassTypeInAdditionalBound.java
 */

class ClassTypeInAdditionalBound {
    class C {}
    interface I {}

    // The additional bound C is a class type; only the first bound may be a class.
    <T extends I & C> void m() {}   // error: unexpected type / type argument not within bounds
}
