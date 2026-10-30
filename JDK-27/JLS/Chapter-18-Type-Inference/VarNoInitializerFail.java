/*
 * @test
 * @summary JLS 14.4.1 / 18: a local variable declared with var must have an
 *          initializer so its type can be inferred.
 * @compile/fail VarNoInitializerFail.java
 */

class VarNoInitializerFail {
    void m() {
        var x;   // error: cannot infer type for local variable x
    }
}
