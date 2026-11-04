/*
 * @test
 * @summary JLS 6.4 (Java SE 8): a local variable may not be redeclared in a
 *          nested block of the same method, because the nested block lies
 *          within the scope of the outer variable.
 * @compile/fail NestedBlockRedeclaresLocal.java
 */

class NestedBlockRedeclaresLocal {
    void m() {
        int x = 1;
        {
            int x = 2;   // error: variable x is already defined in method m()
        }
    }
}
