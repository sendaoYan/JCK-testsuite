/*
 * @test
 * @summary JLS 6.4 (Java SE 27): a formal parameter may not be redeclared as a
 *          local variable of the method body.
 * @compile/fail DuplicateParameterAndLocal.java
 */

class DuplicateParameterAndLocal {
    void m(int x) {
        int x;   // error: variable x is already defined in method m(int)
    }
}
