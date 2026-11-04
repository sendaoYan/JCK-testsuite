/*
 * @test
 * @summary JLS 6.4 (Java SE 27): a local variable may not be redeclared as a
 *          local variable of the directly enclosing method, constructor, or
 *          initializer block within its scope.
 * @compile/fail DuplicateLocalSameBlock.java
 */

class DuplicateLocalSameBlock {
    void m() {
        int x = 1;
        int x = 2;   // error: variable x is already defined in method m()
    }
}
