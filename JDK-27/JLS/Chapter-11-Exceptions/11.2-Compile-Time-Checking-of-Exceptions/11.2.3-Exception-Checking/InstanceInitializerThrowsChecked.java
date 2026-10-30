/*
 * @test
 * @summary JLS 11.2.3: an instance variable initializer or instance initializer of
 *          a named class may not throw a checked exception class unless every
 *          explicitly declared constructor declares it.
 * @compile/fail InstanceInitializerThrowsChecked.java
 */

class InstanceInitializerThrowsChecked {
    {
        throw new Exception();   // error: checked exception in instance initializer
    }
}
