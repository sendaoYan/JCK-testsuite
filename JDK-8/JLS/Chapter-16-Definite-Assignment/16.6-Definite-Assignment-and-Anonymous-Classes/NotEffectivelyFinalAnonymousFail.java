/*
 * @test
 * @summary JLS 4.12.4 / 8.1.3 (Java SE 8): a local variable that is not effectively
 *          final may not be referenced from an anonymous class.
 * @compile/fail NotEffectivelyFinalAnonymousFail.java
 */

class NotEffectivelyFinalAnonymousFail {
    void m() {
        int x = 1;
        x = 2;
        Runnable r = new Runnable() {
            public void run() { System.out.println(x); }   // error: local variables referenced from an inner class must be final or effectively final
        };
    }
}
