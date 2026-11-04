/*
 * @test
 * @summary JLS 15.27.1: the target type of a lambda expression must be a
 *          functional interface; an interface with more than one abstract
 *          method is a compile-time error.
 * @compile/fail LambdaNotFunctionalInterface.java
 */

class LambdaNotFunctionalInterface {
    interface NotAFI { void a(); void b(); }
    void m() {
        NotAFI x = () -> { };   // error: NotAFI is not a functional interface
    }
}
