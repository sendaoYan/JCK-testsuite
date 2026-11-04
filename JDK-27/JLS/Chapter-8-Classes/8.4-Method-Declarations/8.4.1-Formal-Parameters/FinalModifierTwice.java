/*
 * @test
 * @summary JLS 8.4.1: it is a compile-time error if final appears more than once
 *          as a modifier for a formal parameter declaration.
 * @compile/fail FinalModifierTwice.java
 */
class FinalModifierTwice {
    void m(final final int a) { }   // error: repeated modifier
}
