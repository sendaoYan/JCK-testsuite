/*
 * @test
 * @summary JLS 8.9.2: it is a compile-time error if a constructor declaration in
 *          an enum declaration contains a superclass constructor invocation.
 * @compile/fail EnumConstructorSuperInvocation.java
 */

enum Coin {
    PENNY;
    Coin() { super(); }   // error: cannot call super in an enum constructor
}
