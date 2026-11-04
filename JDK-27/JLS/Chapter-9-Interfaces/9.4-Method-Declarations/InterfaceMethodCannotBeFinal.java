/*
 * @test
 * @summary JLS 9.4: it is a compile-time error if an interface method is declared
 *          final.
 * @compile/fail InterfaceMethodCannotBeFinal.java
 */

interface InterfaceMethodCannotBeFinal {
    final void m();   // error: modifier final not allowed here
}
