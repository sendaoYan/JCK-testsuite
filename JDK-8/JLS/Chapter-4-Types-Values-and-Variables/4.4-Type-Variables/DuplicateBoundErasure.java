/*
 * @test
 * @summary JLS 4.4 (Java SE 8): "The erasures of all constituent types of a
 *          bound must be pairwise different, or a compile-time error occurs."
 * @compile/fail DuplicateBoundErasure.java
 */

interface G<T> {}

class DuplicateBoundErasure {
    // G<String> and G<Integer> erase to the same type G, so their erasures are
    // not pairwise different.
    <T extends G<String> & G<Integer>> void m() {}   // error: repeated interface
}
