/*
 * @test
 * @summary JLS 8.9: an enum class is implicitly final, so a class cannot extend an
 *          enum type.
 * @compile/fail EnumCannotBeExtended.java
 */

enum Color {
    RED, GREEN, BLUE
}

class EnumCannotBeExtended extends Color {   // error: cannot inherit from final Color
}
