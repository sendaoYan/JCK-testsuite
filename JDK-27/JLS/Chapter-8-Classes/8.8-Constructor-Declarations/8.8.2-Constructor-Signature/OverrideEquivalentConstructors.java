/*
 * @test
 * @summary JLS 8.8.2: it is a compile-time error to declare two constructors with
 *          override-equivalent signatures in a class.
 * @compile/fail OverrideEquivalentConstructors.java
 */

class OverrideEquivalentConstructors {
    OverrideEquivalentConstructors(int x) {}
    OverrideEquivalentConstructors(int y) {}   // error: duplicate constructor
}
