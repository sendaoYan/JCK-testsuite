/*
 * @test
 * @summary JLS 8.4.3.1: the declaration of an abstract method must appear
 *          directly within an abstract class (unless it occurs in an enum).
 * @compile/fail AbstractMethodInConcreteClass.java
 */

class AbstractMethodInConcreteClass {
    abstract void m();   // error: abstract method in a non-abstract class
}
