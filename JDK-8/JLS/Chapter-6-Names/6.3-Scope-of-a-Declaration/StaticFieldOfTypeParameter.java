/*
 * @test
 * @summary JLS 6.3 (Java SE 8): a class type parameter is not in scope for
 *          static members, so using it as the type of a static field is a
 *          compile-time error.
 * @compile/fail StaticFieldOfTypeParameter.java
 */

class StaticFieldOfTypeParameter<T> {
    static T s;   // error: non-static type variable T cannot be referenced from a static context
}
