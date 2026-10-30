/*
 * @test
 * @summary JLS 8.3.1.2: a blank final instance variable must be definitely
 *          assigned at the end of every constructor of the class.
 * @compile/fail BlankFinalInstanceVariableUnassigned.java
 */

class BlankFinalInstanceVariableUnassigned {
    final int x;
    BlankFinalInstanceVariableUnassigned() {}   // error: x might not be initialized
}
