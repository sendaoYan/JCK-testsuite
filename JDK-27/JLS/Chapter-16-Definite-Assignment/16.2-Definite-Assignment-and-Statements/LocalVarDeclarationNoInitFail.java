/*
 * @test
 * @summary JLS 16.2.4: a local variable declared without an initializer is not
 *          definitely assigned until an assignment statement assigns it, so
 *          reading it before then is a compile-time error.
 * @compile/fail LocalVarDeclarationNoInitFail.java
 */

class LocalVarDeclarationNoInitFail {
    void m() {
        int x;           // no initializer
        int y = x + 1;   // error: variable x might not have been initialized
    }
}
