/*
 * @test
 * @summary JLS 9.3 (Java SE 8): an inherited field name may be ambiguous when a type
 *          inherits two fields of the same name from unrelated interfaces.
 * @compile/fail AmbiguousInheritedField.java
 */

interface A { int X = 1; }
interface B { int X = 2; }

class AmbiguousInheritedField implements A, B {
    int y = X;   // error: reference to X is ambiguous
}
