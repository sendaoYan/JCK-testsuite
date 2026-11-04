/*
 * @test
 * @summary JLS 6.5.6.1 / 15.27.2 (Java SE 8): a local variable used in a lambda
 *          body must be effectively final; reassigning it after capture is a
 *          compile-time error.
 * @compile/fail NotEffectivelyFinalInLambda.java
 */

class NotEffectivelyFinalInLambda {
    void m() {
        int x = 1;
        Runnable r = new Runnable() {
            public void run() {
                int y = x;   // captures x
            }
        };
        x = 2;   // error: local variables referenced from an inner class must be final or effectively final
    }
}
