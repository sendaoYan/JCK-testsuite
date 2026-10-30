/*
 * @test
 * @summary JLS 14.3: a local class or interface declaration may not have the
 *          access modifier public (nor protected or private).
 * @compile/fail LocalClassAccessModifierFail.java
 */

class LocalClassAccessModifierFail {
    void m() {
        public class L {}   // error: modifier public not allowed here
    }
}
