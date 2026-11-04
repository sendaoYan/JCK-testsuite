/*
 * @test
 * @summary JLS 8.4.1 (Java SE 8): it is a compile-time error for a method or constructor to
 *          declare two formal parameters with the same name.
 * @compile/fail DuplicateFormalParameterName.java
 */

class DuplicateFormalParameterName {
    void m(int x, int x) {}   // error: variable x is already defined
}
