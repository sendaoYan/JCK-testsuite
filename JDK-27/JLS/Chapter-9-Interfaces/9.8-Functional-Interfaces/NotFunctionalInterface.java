/*
 * @test
 * @summary JLS 9.8: an interface with more than one abstract method is not a
 *          functional interface and cannot be the target type of a lambda.
 * @compile/fail NotFunctionalInterface.java
 */

interface TwoMethods {
    void m();
    void n();
}

class NotFunctionalInterface {
    void test() {
        TwoMethods t = () -> {};   // error: not a functional interface
    }
}
