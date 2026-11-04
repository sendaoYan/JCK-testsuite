/*
 * @test
 * @summary JLS 4.12.4 / 15.27.2 (Java SE 8): a local variable that is assigned more
 *          than once is not effectively final and may not be captured by a lambda.
 * @compile/fail NotEffectivelyFinalLambdaFail.java
 */

class NotEffectivelyFinalLambdaFail {
    void m() {
        int x = 1;
        x = 2;
        Runnable r = () -> System.out.println(x);   // error: local variables referenced from a lambda must be final or effectively final
    }
}
