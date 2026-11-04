/*
 * @test
 * @summary JLS 8.4.3: it is a compile-time error if the same keyword appears
 *          more than once as a modifier for a method declaration.
 * @compile/fail DuplicateMethodModifier.java
 */
class DuplicateMethodModifier {
    public public void m() { }   // error: repeated modifier
}
