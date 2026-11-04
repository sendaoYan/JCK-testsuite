/*
 * @test
 * @summary JLS 8.7 (Java SE 8): it is a compile-time error for a static initializer
 *          to be able to complete abruptly with a checked exception.
 * @compile/fail StaticInitializerCheckedException.java
 */

class StaticInitializerCheckedException {
    static {
        throwIt();   // error: unreported exception IOException; must be caught or declared to be thrown
    }

    static void throwIt() throws java.io.IOException {}
}
