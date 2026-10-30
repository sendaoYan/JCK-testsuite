/*
 * @test
 * @summary JLS 11.2.3: a class variable initializer or static initializer of a
 *          named class may not throw a checked exception class.
 * @compile/fail StaticInitializerThrowsChecked.java
 */

class StaticInitializerThrowsChecked {
    static {
        throw new Exception();   // error: checked exception in static initializer
    }
}
