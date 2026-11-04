/*
 * @test
 * @summary JLS 15.9.1: "It is a compile-time error if the class being
 *          instantiated is an abstract class."  new of an abstract class fails.
 * @compile/fail AbstractClassInstantiation.java
 */

class AbstractClassInstantiation {
    abstract static class Shape { }
    Object m() {
        return new Shape();   // error: Shape is abstract; cannot be instantiated
    }
}
