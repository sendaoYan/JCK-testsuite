/*
 * @test
 * @summary JLS 8.8.1: the formal parameters of a constructor follow the rules of
 *          method formal parameters, including uniqueness of parameter names.
 * @compile/fail DuplicateConstructorParameterName.java
 */

class DuplicateConstructorParameterName {
    DuplicateConstructorParameterName(int x, int x) {}   // error: duplicate parameter
}
