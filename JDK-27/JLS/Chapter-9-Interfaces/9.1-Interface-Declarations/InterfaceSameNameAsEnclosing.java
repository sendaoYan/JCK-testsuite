/*
 * @test
 * @summary JLS 9.1: it is a compile-time error if an interface has the same simple
 *          name as any of its enclosing classes or interfaces.
 * @compile/fail InterfaceSameNameAsEnclosing.java
 */

class Outer {
    interface Outer {}   // error: duplicate name within Outer
}
