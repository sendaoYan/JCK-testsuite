/*
 * @test
 * @summary JLS 8.4.3.4 / 8.4.7: the body of a native method is a semicolon only;
 *          giving it a block body is a compile-time error.
 * @compile/fail NativeMethodWithBody.java
 */

class NativeMethodWithBody {
    native void m() { }   // error: native method cannot have a body
}
