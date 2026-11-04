/*
 * @test
 * @summary JLS 16.2.4 (Java SE 8): a local variable declared without an initializer
 *          is not definitely assigned until an assignment statement assigns it.
 * @compile/fail LocalVarDeclarationNoInitFail.java
 */

class LocalVarDeclarationNoInitFail {
    void m() {
        int x;
        int y = x + 1;   // error: variable x might not have been initialized
    }
}
