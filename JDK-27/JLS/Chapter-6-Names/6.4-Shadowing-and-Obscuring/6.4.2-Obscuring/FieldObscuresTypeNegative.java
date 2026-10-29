/*
 * @test
 * @summary JLS 6.4.2: a field named System obscures the type java.lang.System,
 *          so the unqualified System.out fails to compile.
 * @compile/fail FieldObscuresTypeNegative.java
 */

class FieldObscuresTypeNegative {
    static String System = "";
    void m() {
        System.out.println("hi");   // error: System is a field, not a type
    }
}
