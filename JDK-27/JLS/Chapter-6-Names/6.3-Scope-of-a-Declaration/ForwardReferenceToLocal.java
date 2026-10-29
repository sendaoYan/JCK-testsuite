/*
 * @test
 * @summary JLS 6.3 Example 6.3-2: the initializer of a local variable is within
 *          the scope of that variable but it has no value yet, so "int x = x;"
 *          is a compile-time error.
 * @compile/fail ForwardReferenceToLocal.java
 */

class ForwardReferenceToLocal {
    static int x;
    void m() {
        int x = x;   // error: local x has no value in its own initializer
    }
}
