/*
 * @test
 * @summary JLS 5.2 (Java SE 8): "It is a compile-time error if the chain of
 *          conversions contains two parameterized types that are not in the
 *          subtype relation." (e.g. Comparable<Integer> -> Comparable ->
 *          Comparable<String> is not a valid assignment conversion.)
 * @compile/fail InvalidChainTwoParameterizedTypes.java
 */

class InvalidChainTwoParameterizedTypes {
    void m() {
        Comparable<Integer> ci = Integer.valueOf(1);
        Comparable<String> cs = ci;   // error: incompatible types
    }
}
