/*
 * @test
 * @summary JLS 8.4.7: an abstract method declaration has a body consisting only
 *          of a semicolon; giving it a block body is a compile-time error.
 * @compile/fail AbstractMethodWithBody.java
 */

abstract class AbstractMethodWithBody {
    abstract void m() {}   // error: abstract method cannot have a body
}
