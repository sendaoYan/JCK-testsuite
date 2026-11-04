/*
 * @test
 * @summary JLS 8.3.3: a reference by simple name to a class variable from a class
 *          variable initializer at a point to the left of its declarator is a
 *          compile-time error.
 * @compile/fail StaticForwardReferenceViaSimpleName.java
 */
class StaticForwardReferenceViaSimpleName {
    static int i = j + 2;   // error: illegal forward reference
    static int j = 4;
}
