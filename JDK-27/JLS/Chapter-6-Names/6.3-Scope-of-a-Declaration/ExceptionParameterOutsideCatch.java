/*
 * @test
 * @summary JLS 6.3 (Java SE 27): an exception parameter is only in scope within
 *          its catch clause, so using it after the clause is a compile-time
 *          error.
 * @compile/fail ExceptionParameterOutsideCatch.java
 */

class ExceptionParameterOutsideCatch {
    void m() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            // e in scope here
        }
        e.printStackTrace();   // error: cannot find symbol e
    }
}
