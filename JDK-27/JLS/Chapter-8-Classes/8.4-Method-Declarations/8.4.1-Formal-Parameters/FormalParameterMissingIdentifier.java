/*
 * @test
 * @summary JLS 8.4.1: every declaration of a formal parameter must include an
 *          Identifier.
 * @compile/fail FormalParameterMissingIdentifier.java
 */
class FormalParameterMissingIdentifier {
    void m(int) { }   // error: <identifier> expected
}
