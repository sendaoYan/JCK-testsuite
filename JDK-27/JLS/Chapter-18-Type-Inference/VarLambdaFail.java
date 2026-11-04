/*
 * @test
 * @summary JLS 14.4.1 / 18: a local variable declared with var cannot be
 *          initialized with a lambda (or method reference) because it needs an
 *          explicit target type.
 * @compile/fail VarLambdaFail.java
 */

class VarLambdaFail {
    void m() {
        var x = () -> {};   // error: cannot infer type for local variable x (lambda needs a target type)
    }
}
