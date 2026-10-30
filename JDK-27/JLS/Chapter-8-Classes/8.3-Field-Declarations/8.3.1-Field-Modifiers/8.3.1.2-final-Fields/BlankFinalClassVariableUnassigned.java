/*
 * @test
 * @summary JLS 8.3.1.2: a blank final class variable must be definitely assigned
 *          by a static initializer of the class in which it is declared.
 * @compile/fail BlankFinalClassVariableUnassigned.java
 */

class BlankFinalClassVariableUnassigned {
    static final int x;   // error: not assigned in any static initializer
}
