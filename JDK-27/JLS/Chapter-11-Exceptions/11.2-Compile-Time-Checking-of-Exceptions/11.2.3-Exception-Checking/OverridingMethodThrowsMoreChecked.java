/*
 * @test
 * @summary JLS 11.2.3: an overriding method may not throw a checked exception not
 *          permitted by the throws clause of the overridden method.
 * @compile/fail OverridingMethodThrowsMoreChecked.java
 */

import java.io.IOException;

class Base {
    void m() {}
}

class OverridingMethodThrowsMoreChecked extends Base {
    void m() throws IOException {}   // error: overridden method does not throw IOException
}
