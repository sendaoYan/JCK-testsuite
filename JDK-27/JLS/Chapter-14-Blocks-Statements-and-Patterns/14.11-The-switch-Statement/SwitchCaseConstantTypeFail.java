/*
 * @test
 * @summary JLS 14.11: every case constant of a switch statement must be assignable
 *          to the type of the selector expression; a long case constant for an int
 *          selector is a compile-time error.
 * @compile/fail SwitchCaseConstantTypeFail.java
 */

class SwitchCaseConstantTypeFail {
    void m(int v) {
        switch (v) {
            case 1L: break;   // error: incompatible types: possible lossy conversion from long to int
        }
    }
}
