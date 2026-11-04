/*
 * @test
 * @summary JLS 8.4.3 (Java SE 8): it is a compile-time error if an abstract method is also
 *          declared final.
 * @compile/fail AbstractFinalMethod.java
 */
abstract class AbstractFinalMethod {
    abstract final void m();   // error: illegal combination of modifiers: abstract and final
}
