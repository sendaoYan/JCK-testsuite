/*
 * @test
 * @summary JLS 15.9.1 (Java SE 8): "It is a compile-time error if the class
 *          being instantiated is an abstract class."
 * @compile/fail AbstractClassInstantiation.java
 */

class AbstractClassInstantiation {
    abstract static class Shape { }
    Object m() {
        return new Shape();   // error: Shape is abstract; cannot be instantiated
    }
}
