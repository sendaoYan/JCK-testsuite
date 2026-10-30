/*
 * @test
 * @summary JLS 14.18: the expression of a throw statement must be assignable to
 *          Throwable (or be the null reference).
 * @compile/fail ThrowNonThrowableFail.java
 */

class ThrowNonThrowableFail {
    void m() {
        throw new Object();   // error: Object cannot be converted to Throwable
    }
}
