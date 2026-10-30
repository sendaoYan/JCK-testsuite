/*
 * @test
 * @summary JLS 8.4.1: at most one variable arity parameter is permitted, and it
 *          must appear only in the last position of the parameter list.
 * @compile/fail VariableArityParameterNotLast.java
 */

class VariableArityParameterNotLast {
    void m(int... xs, int y) {}   // error: varargs parameter must be last
}
