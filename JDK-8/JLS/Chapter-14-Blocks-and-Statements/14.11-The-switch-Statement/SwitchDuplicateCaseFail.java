/*
 * @test
 * @summary JLS 14.11.1: no two case constants associated with a switch block may
 *          have the same value.
 * @compile/fail SwitchDuplicateCaseFail.java
 */

class SwitchDuplicateCaseFail {
    void m(int v) {
        switch (v) {
            case 1: break;
            case 1: break;   // error: duplicate case label
        }
    }
}
