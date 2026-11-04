/*
 * @test
 * @summary JLS 9.3.1 (Java SE 8): a field initializer in an interface may not refer to
 *          a field declared later by simple name.
 * @compile/fail InterfaceFieldForwardReference.java
 */

interface InterfaceFieldForwardReference {
    int A = B;   // error: illegal forward reference
    int B = 1;
}
