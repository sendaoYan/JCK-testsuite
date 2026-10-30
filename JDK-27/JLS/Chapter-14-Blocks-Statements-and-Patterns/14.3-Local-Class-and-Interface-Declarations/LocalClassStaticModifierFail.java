/*
 * @test
 * @summary JLS 14.3: a local class or interface declaration may not have the
 *          modifier static.
 * @compile/fail LocalClassStaticModifierFail.java
 */

class LocalClassStaticModifierFail {
    void m() {
        static class L {}   // error: modifier static not allowed here
    }
}
