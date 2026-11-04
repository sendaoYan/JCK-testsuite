/*
 * @test
 * @summary JLS 8.3.1: it is a compile-time error if the same keyword appears
 *          more than once as a modifier for a field declaration.
 * @compile/fail DuplicateFieldModifier.java
 */
class DuplicateFieldModifier {
    public public int f;   // error: repeated modifier
}
