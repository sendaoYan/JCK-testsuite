/*
 * @test
 * @summary JLS 6.3 (Java SE 27): a local variable is not in scope before its
 *          declaration in the same block, so a forward reference is a
 *          compile-time error.
 * @compile/fail LocalVariableBeforeDeclaration.java
 */

class LocalVariableBeforeDeclaration {
    void m() {
        int y = x;   // error: cannot find symbol x
        int x = 1;
    }
}
