/*
 * @test
 * @summary JLS 8.4.3: a method declaration that contains native may not also
 *          contain strictfp.
 * @compile/fail NativeWithStrictfp.java
 */

class NativeWithStrictfp {
    native strictfp void m();   // error: illegal combination of modifiers
}
