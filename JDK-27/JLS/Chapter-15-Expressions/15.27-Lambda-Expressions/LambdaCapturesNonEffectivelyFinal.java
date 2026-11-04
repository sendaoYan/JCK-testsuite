/*
 * @test
 * @summary JLS 15.27.2: a lambda expression may only capture local variables
 *          that are effectively final; a variable reassigned after being
 *          referenced is a compile-time error.
 * @compile/fail LambdaCapturesNonEffectivelyFinal.java
 */

class LambdaCapturesNonEffectivelyFinal {
    void m() {
        int x = 1;
        Runnable r = () -> { int y = x; };   // error: local variables referenced from a lambda must be final or effectively final
        x = 2;
    }
}
