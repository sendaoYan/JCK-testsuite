/*
 * @test
 * @summary JLS 6.5.5.1 Example 6.5.5.1-1: a class type parameter must not appear
 *          in a static context. Box<T> in a static method is a compile-time
 *          error.
 * @compile/fail ClassTypeParameterInStaticMethod.java
 */

class ClassTypeParameterInStaticMethod<T> {
    T val;
    static ClassTypeParameterInStaticMethod<T> empty() {   // error: T in static context
        return null;
    }
}
