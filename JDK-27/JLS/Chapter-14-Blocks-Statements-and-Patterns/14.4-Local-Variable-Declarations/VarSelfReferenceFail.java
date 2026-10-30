/*
 * @test
 * @summary JLS 14.4: the initializer of a var declaration may not reference the
 *          variable being declared.
 * @compile/fail VarSelfReferenceFail.java
 */

class VarSelfReferenceFail {
    void m() {
        var g = (g = 7);   // error: self-reference in initializer of var
    }
}
