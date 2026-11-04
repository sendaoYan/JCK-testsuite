/*
 * @test
 * @summary JLS 9.4.3: a private static interface method is not abstract, so it must
 *          have a block body; a semicolon body is a compile-time error.
 * @compile/fail PrivateStaticInterfaceMethodSemicolonBody.java
 */

interface PrivateStaticInterfaceMethodSemicolonBody {
    private static void m();   // error: method m() is missing a method body, or should be declared abstract
}
