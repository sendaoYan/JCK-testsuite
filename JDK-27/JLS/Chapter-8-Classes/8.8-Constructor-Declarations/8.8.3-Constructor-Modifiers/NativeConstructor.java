/*
 * @test
 * @summary JLS 8.8.3: a constructor cannot be native.
 * @compile/fail NativeConstructor.java
 */

class NativeConstructor {
    native NativeConstructor();   // error: modifier native not allowed here
}
