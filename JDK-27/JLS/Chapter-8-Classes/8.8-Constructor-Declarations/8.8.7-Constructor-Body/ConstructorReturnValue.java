/*
 * @test
 * @summary JLS 8.8.7: a return statement with an expression is prohibited in a
 *          constructor body.
 * @compile/fail ConstructorReturnValue.java
 */

class ConstructorReturnValue {
    ConstructorReturnValue() {
        return 1;   // error: cannot return a value from a constructor
    }
}
