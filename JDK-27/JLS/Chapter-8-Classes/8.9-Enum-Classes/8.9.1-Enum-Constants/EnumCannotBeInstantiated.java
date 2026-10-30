/*
 * @test
 * @summary JLS 8.9: an enum class has no instances other than those defined by
 *          its enum constants; it cannot be explicitly instantiated.
 * @compile/fail EnumCannotBeInstantiated.java
 */

enum Color { RED, GREEN, BLUE }

class EnumCannotBeInstantiated {
    void m() {
        new Color();   // error: enum types may not be instantiated
    }
}
