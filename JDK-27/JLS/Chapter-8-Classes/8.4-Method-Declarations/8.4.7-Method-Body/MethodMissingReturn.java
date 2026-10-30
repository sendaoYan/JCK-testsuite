/*
 * @test
 * @summary JLS 8.4.7: a method whose return type is not void must return a value
 *          on every path that completes normally.
 * @compile/fail MethodMissingReturn.java
 */

class MethodMissingReturn {
    int m() {
        // no return statement
    }
}
