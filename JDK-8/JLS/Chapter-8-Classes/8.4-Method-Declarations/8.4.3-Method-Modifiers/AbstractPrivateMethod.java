/*
 * @test
 * @summary JLS 8.4.3 (Java SE 8): it is a compile-time error if an abstract method is also
 *          declared private.
 * @compile/fail AbstractPrivateMethod.java
 */
abstract class AbstractPrivateMethod {
    abstract private void m();   // error: illegal combination of modifiers: abstract and private
}
