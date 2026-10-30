/*
 * @test
 * @summary JLS 8.1.3: a local variable used but not declared in an inner class
 *          must be final or effectively final.
 * @compile/fail InnerClassCaptureNotEffectivelyFinal.java
 */

class Outer {
    void m() {
        int v = 1;
        v = 2;                       // not effectively final
        class Local {
            int k = v;               // error: v is not effectively final
        }
    }
}
