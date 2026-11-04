/*
 * @test
 * @summary JLS 6.3 (Java SE 27): the scope of a for-loop variable ends with the
 *          for statement, so using it after the loop is a compile-time error.
 * @compile/fail ForVariableAfterLoop.java
 */

class ForVariableAfterLoop {
    void m() {
        for (int i = 0; i < 3; i++) { }
        int y = i;   // error: cannot find symbol i
    }
}
