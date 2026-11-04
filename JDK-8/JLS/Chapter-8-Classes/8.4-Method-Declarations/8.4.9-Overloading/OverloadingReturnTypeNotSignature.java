/*
 * @test
 * @summary JLS 8.4.9 / 8.4.2: the return type is not part of a method signature,
 *          so two methods that differ only in return type are not distinct.
 * @compile/fail OverloadingReturnTypeNotSignature.java
 */
class OverloadingReturnTypeNotSignature {
    int m() { return 1; }
    void m() { }   // error: method m() is already defined
}
