/*
 * @test
 * @summary JLS 6.4.2 (Java SE 27): a local variable obscures a type name, so in a
 *          context where the name is classified as an expression name the type
 *          is unreachable and member access fails.
 * @compile/fail LocalVariableObscuresTypeNegative.java
 */

class LocalVariableObscuresTypeNegative {
    void m() {
        int System = 1;        // local variable obscures the type java.lang.System
        System.out.println();  // error: int cannot be dereferenced (System is the local)
    }
}
