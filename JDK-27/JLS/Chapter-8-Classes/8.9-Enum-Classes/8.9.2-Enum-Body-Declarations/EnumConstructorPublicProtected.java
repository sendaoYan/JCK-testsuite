/*
 * @test
 * @summary JLS 8.9.2: it is a compile-time error if a constructor declaration in
 *          an enum declaration is public or protected.
 * @compile/fail EnumConstructorPublicProtected.java
 */

enum Coin {
    PENNY(1);
    public Coin(int value) {}   // error: enum constructor cannot be public
}
