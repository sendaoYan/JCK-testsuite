/*
 * @test
 * @summary JLS 8.9 (Java SE 8): the direct superclass of an enum type is Enum<E>, and
 *          an enum declaration may not have an explicit extends clause.
 * @compile/fail EnumCannotExtendOtherClass.java
 */

class Base {}

enum EnumCannotExtendOtherClass extends Base {   // error: an enum declaration cannot have an extends clause
    A
}
