/*
 * @test
 * @summary JLS 11.1.1: unchecked exception classes (RuntimeException and Error and
 *          their subclasses) are exempt from compile-time checking.
 * @compile UncheckedExceptionNoDeclaration.java
 */

class UncheckedExceptionNoDeclaration {
    void m() {
        throw new RuntimeException();   // no throws clause required
    }

    void n() {
        throw new Error();              // no throws clause required
    }
}
