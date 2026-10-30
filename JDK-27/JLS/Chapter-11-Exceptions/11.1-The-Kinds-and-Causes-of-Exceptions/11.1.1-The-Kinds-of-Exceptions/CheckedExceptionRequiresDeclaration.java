/*
 * @test
 * @summary JLS 11.1.1: a checked exception class cannot be thrown from a method or
 *          constructor that does not declare it.
 * @compile/fail CheckedExceptionRequiresDeclaration.java
 */

class CheckedExceptionRequiresDeclaration {
    void m() {
        throw new Exception();   // error: unreported checked exception
    }
}
