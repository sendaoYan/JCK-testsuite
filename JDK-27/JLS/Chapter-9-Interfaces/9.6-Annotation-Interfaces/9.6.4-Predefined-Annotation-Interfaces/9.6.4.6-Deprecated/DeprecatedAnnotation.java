/*
 * @test
 * @summary JLS 9.6.4.6: Deprecated marks a declaration for which use is
 *          discouraged; a deprecated API can still be referenced.
 * @compile DeprecatedAnnotation.java
 */

@Deprecated
class OldApi {}

class DeprecatedAnnotation {
    @SuppressWarnings("deprecation")
    void use() { new OldApi(); }
}
