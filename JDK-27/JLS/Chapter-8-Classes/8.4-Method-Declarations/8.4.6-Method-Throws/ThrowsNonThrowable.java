/*
 * @test
 * @summary JLS 8.4.6: it is a compile-time error if a named class type listed in
 *          a throws clause is not a subtype of Throwable.
 * @compile/fail ThrowsNonThrowable.java
 */

class ThrowsNonThrowable {
    void m() throws String {}   // error: incompatible types
}
