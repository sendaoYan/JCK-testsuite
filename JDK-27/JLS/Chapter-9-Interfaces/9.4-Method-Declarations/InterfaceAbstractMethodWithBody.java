/*
 * @test
 * @summary JLS 9.4: an abstract interface method has a semicolon body; giving it a
 *          block body is a compile-time error.
 * @compile/fail InterfaceAbstractMethodWithBody.java
 */

interface InterfaceAbstractMethodWithBody {
    abstract void m() {}   // error: abstract method cannot have a body
}
