/*
 * @test
 * @summary JLS 6.3 (Java SE 8): a local class is not in scope before its
 *          declaration, so a forward reference is a compile-time error.
 * @compile/fail LocalClassBeforeDeclaration.java
 */

class LocalClassBeforeDeclaration {
    void m() {
        L l = new L();   // error: cannot find symbol L
        class L { }
    }
}
