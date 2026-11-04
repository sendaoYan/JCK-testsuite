/*
 * @test
 * @summary JLS 8.4.3 (Java SE 8): it is a compile-time error if an abstract method is also
 *          declared synchronized.
 * @compile/fail AbstractSynchronizedMethod.java
 */
abstract class AbstractSynchronizedMethod {
    abstract synchronized void m();   // error: illegal combination of modifiers: abstract and synchronized
}
