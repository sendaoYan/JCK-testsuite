/*
 * @test
 * @summary JLS 8.1.4: the class Enum can only be extended by an enum class;
 *          extending it from a normal class is a compile-time error.
 * @compile/fail ExtendEnumClass.java
 */

class ExtendEnumClass extends Enum<ExtendEnumClass> {}   // error: cannot extend Enum
