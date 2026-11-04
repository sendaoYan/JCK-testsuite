/*
 * @test
 * @summary JLS 8.9 (Java SE 8): an enum type is implicitly final, so a class cannot
 *          extend an enum type.
 * @compile/fail EnumCannotBeExtended.java
 */

enum Color {
    RED, GREEN, BLUE
}

class EnumCannotBeExtended extends Color {   // error: cannot inherit from final Color
}
