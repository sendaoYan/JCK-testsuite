/*
 * @test
 * @summary JLS 14.4: when LocalVariableType is var, more than one
 *          VariableDeclarator is not allowed.
 * @compile/fail VarRestrictionsFail.java
 */

class VarRestrictionsFail {
    void m() {
        var a = 1, b = 2;   // error: var cannot be used with multiple declarators
    }
}
