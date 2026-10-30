/*
 * @test
 * @summary JLS 14.11: the selector expression of a switch statement may not have
 *          type boolean, long, float or double.
 * @compile/fail SwitchCompatibilityFail.java
 */

class SwitchCompatibilityFail {
    void m(long v) {
        switch (v) {        // error: long not allowed as switch selector
            case 1L: break;
        }
    }
}
