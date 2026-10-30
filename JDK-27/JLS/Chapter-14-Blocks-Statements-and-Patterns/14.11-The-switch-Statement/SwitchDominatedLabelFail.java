/*
 * @test
 * @summary JLS 14.11.1: a case label is dominated if a preceding unguarded case
 *          label with a pattern would apply to every value it applies to.
 * @compile/fail SwitchDominatedLabelFail.java
 */

class SwitchDominatedLabelFail {
    void m(Object o) {
        switch (o) {
            case Object obj -> { }   // unconditional
            case String s -> { }     // error: dominated by Object obj
            default -> { }
        }
    }
}
