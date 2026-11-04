/*
 * @test
 * @summary JLS 6.4.2 (Java SE 8): a field named System obscures the type
 *          java.lang.System, so the unqualified System.out.println fails to
 *          compile (System is a field, not a type).
 * @compile/fail FieldObscuresTypeNegative.java
 */

class FieldObscuresTypeNegative {
    static String System = "";
    void m() {
        System.out.println("hi");   // error: cannot find symbol (System is a field)
    }
}
