/*
 * @test
 * @summary JLS 8.6: an instance initializer that completes abruptly with a checked
 *          exception must be matched by the throws clauses of the constructors.
 * @compile/fail InstanceInitializerCheckedException.java
 */

class InstanceInitializerCheckedException {
    {
        throwIt();   // error: unreported exception IOException; must be caught or declared to be thrown
    }

    InstanceInitializerCheckedException() {}

    void throwIt() throws java.io.IOException {}
}
