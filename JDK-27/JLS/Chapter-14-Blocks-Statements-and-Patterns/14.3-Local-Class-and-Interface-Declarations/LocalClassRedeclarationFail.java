/*
 * @test
 * @summary JLS 14.3 (Example 14.3-1): a local class name cannot be redeclared
 *          within the same method, constructor or initializer.
 * @compile/fail LocalClassRedeclarationFail.java
 */

class LocalClassRedeclarationFail {
    void m() {
        class L {}
        class L {}   // error: class L is already defined in method m()
    }
}
