/*
 * @test
 * @summary JLS 6.5.5.1 Example 6.5.5.1-1: a method type parameter must not appear
 *          in a static context, e.g. in the body of an implicitly static local
 *          interface declared in the method.
 * @compile/fail MethodTypeParameterInLocalInterface.java
 */

class MethodTypeParameterInLocalInterface<T> {
    static <U> MethodTypeParameterInLocalInterface<U> make(U val) {
        interface Checker {
            void check(U val);   // error: U in a static context (local interface)
        }
        return null;
    }
}
