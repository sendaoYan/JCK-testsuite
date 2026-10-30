/*
 * @test
 * @summary JLS 8.3.3 (Java SE 8): an instance variable cannot be referenced in a
 *          textual forward reference in its own initializer.
 * @compile/fail ForwardReferenceTest.java
 */

class ForwardReferenceTest {
    int a = b;   // error: illegal forward reference
    int b = 1;
}
