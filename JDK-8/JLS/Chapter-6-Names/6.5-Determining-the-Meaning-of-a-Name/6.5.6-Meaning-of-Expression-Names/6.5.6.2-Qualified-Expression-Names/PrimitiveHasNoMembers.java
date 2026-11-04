/*
 * @test
 * @summary JLS 6.5.6.2 (Java SE 8): it is a compile-time error if the type of the
 *          qualifier of a qualified expression name has no members (e.g. a
 *          primitive type), so int-valued expressions have no fields.
 * @compile/fail PrimitiveHasNoMembers.java
 */

class PrimitiveHasNoMembers {
    void m() {
        int x = 1;
        int y = x.someField;   // error: int cannot be dereferenced
    }
}
