/*
 * @test
 * @summary JLS 5.2: "It is a compile-time error if the chain of conversions
 *          contains two parameterized types that are not in the subtype
 *          relation." Assigning an Integer to a Comparable<String> would chain
 *          Integer -> Comparable<Integer> -> Comparable -> Comparable<String>,
 *          which is illegal because Comparable<Integer> and Comparable<String>
 *          are not subtypes.
 * @compile/fail InvalidAssignmentConversionChain.java
 */

class InvalidAssignmentConversionChain {
    void m() {
        Integer i = 1;
        Comparable<String> c = i;   // error: illegal conversion chain
    }
}
