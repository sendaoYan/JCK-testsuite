/*
 * @test
 * @summary JLS 13.4.9/4.12.4 (Java SE 8): a static final field whose initializer
 *          is not a constant expression is not a constant variable and cannot be
 *          used as a switch case label.
 * @compile/fail NonConstantCaseLabel.java
 */

class NonConstantCaseLabel {
    static final int NOT_CONST = Integer.parseInt("7");
    void m(int x) {
        switch (x) {
            case NOT_CONST: break;   // error: constant expression required
        }
    }
}
