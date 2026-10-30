/*
 * @test
 * @summary JLS 16 (Java SE 8): a blank final instance variable must be definitely
 *          assigned by the end of every constructor.
 * @compile/fail BlankFinalFail.java
 */

class BlankFinalFail {
    final int x;   // blank final never assigned
    BlankFinalFail() {}
}
