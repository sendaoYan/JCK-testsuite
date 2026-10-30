/*
 * @test
 * @summary JLS 9.3 (Example 9.3-1): if an interface inherits two fields with the
 *          same name, any reference to the field by simple name is ambiguous and
 *          causes a compile-time error.
 * @compile/fail AmbiguousInheritedField.java
 */

interface BaseColors {
    int RED = 1, GREEN = 2, BLUE = 4;
}
interface RainbowColors extends BaseColors {
    int YELLOW = 3, ORANGE = 5, INDIGO = 6, VIOLET = 7;
}
interface PrintColors extends BaseColors {
    int YELLOW = 8, CYAN = 16, MAGENTA = 32;
}
interface AmbiguousInheritedField extends RainbowColors, PrintColors {
    int FUCHSIA = 17, VERMILION = 43, CHARTREUSE = YELLOW + 90;   // error: ambiguous YELLOW
}
