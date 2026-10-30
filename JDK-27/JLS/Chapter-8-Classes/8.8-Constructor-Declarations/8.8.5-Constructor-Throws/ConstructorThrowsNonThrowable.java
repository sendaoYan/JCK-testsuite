/*
 * @test
 * @summary JLS 8.8.5: the throws clause of a constructor behaves like that of a
 *          method, so a named class type must be a subtype of Throwable.
 * @compile/fail ConstructorThrowsNonThrowable.java
 */

class ConstructorThrowsNonThrowable {
    ConstructorThrowsNonThrowable() throws String {}   // error: incompatible types
}
