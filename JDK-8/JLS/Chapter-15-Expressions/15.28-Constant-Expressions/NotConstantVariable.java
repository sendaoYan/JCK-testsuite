/*
 * @test
 * @summary JLS 15.28 (Java SE 8): a case label must be a constant expression or
 *          an enum constant; a non-constant local variable is a compile-time
 *          error.
 * @compile/fail NotConstantVariable.java
 */

class NotConstantVariable {
    void m(int x) {
        int y = 1;            // not final, hence not a constant variable
        switch (x) {
            case y: break;    // error: constant expression required
        }
    }
}
