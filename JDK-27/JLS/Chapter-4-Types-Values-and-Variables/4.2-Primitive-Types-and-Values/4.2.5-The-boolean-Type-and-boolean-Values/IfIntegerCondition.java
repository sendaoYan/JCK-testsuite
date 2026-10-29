/*
 * @test
 * @summary JLS 4.2.5: "Only boolean and Boolean expressions can be used in
 *          control flow statements." An integer expression may NOT be used as
 *          the condition of an if statement; this must be a compile-time error.
 * @compile/fail IfIntegerCondition.java
 */

class IfIntegerCondition {
    void m(int x) {
        if (x) {   // error: int is not a valid condition
        }
    }
}
