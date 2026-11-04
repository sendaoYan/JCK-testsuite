/*
 * @test
 * @summary JLS 14.4.1 / 18: a local variable declared with var cannot be
 *          initialized with null, because its type cannot be inferred.
 * @compile/fail VarNullFail.java
 */

class VarNullFail {
    void m() {
        var x = null;   // error: cannot infer type for local variable x
    }
}
