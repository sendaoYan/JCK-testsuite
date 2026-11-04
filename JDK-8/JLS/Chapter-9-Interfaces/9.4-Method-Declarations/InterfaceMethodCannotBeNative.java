/*
 * @test
 * @summary JLS 9.4 (Java SE 8): it is a compile-time error if an interface method is
 *          declared native.
 * @compile/fail InterfaceMethodCannotBeNative.java
 */

interface InterfaceMethodCannotBeNative {
    native void m();   // error: modifier native not allowed here
}
