/*
 * @test
 * @summary JLS 4.4: "It is a compile-time error if any of the types I1, ..., In
 *          is a class type or type variable." An additional bound in a type
 *          parameter bound must be an interface, not a class.
 * @compile/fail ClassTypeInAdditionalBound.java
 */

class C1 {}
class C2 {}

class ClassTypeInAdditionalBound {
    // C2 is a class type, so it may not appear as an additional bound.
    <T extends C1 & C2> void m(T t) {}
}
