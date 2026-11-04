/*
 * @test
 * @summary JLS 4.4 (Java SE 8): "A type variable must not at the same time be a
 *          subtype of two interface types which are different parameterizations
 *          of the same generic interface, or a compile-time error occurs."
 * @compile/fail TwoParameterizationsOfSameInterface.java
 */

interface Box<T> {}

class TwoParameterizationsOfSameInterface {
    // Box<String> and Box<Integer> are different parameterizations of Box.
    <T extends Box<String> & Box<Integer>> void m() {}   // error: incompatible types
}
