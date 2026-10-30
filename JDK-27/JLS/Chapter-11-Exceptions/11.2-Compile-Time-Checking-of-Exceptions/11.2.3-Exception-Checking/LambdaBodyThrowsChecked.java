/*
 * @test
 * @summary JLS 11.2.3: a lambda body may not throw a checked exception class not
 *          declared by the function type targeted by the lambda expression.
 * @compile/fail LambdaBodyThrowsChecked.java
 */

class LambdaBodyThrowsChecked {
    interface Call { void call(); }

    void m() {
        Call c = () -> { throw new Exception(); };   // error: unreported exception
    }
}
