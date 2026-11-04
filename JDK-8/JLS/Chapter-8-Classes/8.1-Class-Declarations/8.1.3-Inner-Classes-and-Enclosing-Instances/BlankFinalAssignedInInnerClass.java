/*
 * @test
 * @summary JLS 8.1.3 (Java SE 8): a blank final field of a lexically enclosing class may not
 *          be assigned within an inner class.
 * @compile/fail BlankFinalAssignedInInnerClass.java
 */
class BlankFinalAssignedInInnerClass {
    final int x;
    BlankFinalAssignedInInnerClass() { x = 0; }
    class Inner {
        void m() { x = 3; }   // error: cannot assign a value to final variable x
    }
}
