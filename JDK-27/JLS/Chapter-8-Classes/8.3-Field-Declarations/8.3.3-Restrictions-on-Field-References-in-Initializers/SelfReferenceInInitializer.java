/*
 * @test
 * @summary JLS 8.3.3: a field used in its own initializer is a compile-time
 *          error (self-reference).
 * @compile/fail SelfReferenceInInitializer.java
 */
class SelfReferenceInInitializer {
    int i = i;   // error: illegal forward reference (self reference)
}
