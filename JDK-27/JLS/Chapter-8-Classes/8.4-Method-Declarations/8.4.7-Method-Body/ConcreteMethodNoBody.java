/*
 * @test
 * @summary JLS 8.4.7: a non-abstract, non-native method must have a block body,
 *          not merely a semicolon.
 * @compile/fail ConcreteMethodNoBody.java
 */

class ConcreteMethodNoBody {
    void m();   // error: missing method body
}
