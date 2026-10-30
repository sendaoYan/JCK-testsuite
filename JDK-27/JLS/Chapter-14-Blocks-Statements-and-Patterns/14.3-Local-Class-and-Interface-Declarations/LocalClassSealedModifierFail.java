/*
 * @test
 * @summary JLS 14.3: a local class or interface declaration may not have the
 *          modifier sealed or non-sealed.
 * @compile/fail LocalClassSealedModifierFail.java
 */

class LocalClassSealedModifierFail {
    void m() {
        sealed class L {}   // error: sealed not allowed on a local class
    }
}
