/*
 * @test
 * @summary JLS 8.1.1: it is a compile-time error if the same keyword appears
 *          more than once as a modifier for a class declaration.
 * @compile/fail DuplicateClassModifier.java
 */
class DuplicateClassModifier {
    public public class Inner { }   // error: repeated modifier
}
