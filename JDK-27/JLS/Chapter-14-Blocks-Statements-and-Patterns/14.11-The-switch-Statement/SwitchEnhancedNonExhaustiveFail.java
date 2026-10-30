/*
 * @test
 * @summary JLS 14.11.2: an enhanced switch statement must be exhaustive.
 * @compile/fail SwitchEnhancedNonExhaustiveFail.java
 */

class SwitchEnhancedNonExhaustiveFail {
    void m(Object o) {
        switch (o) {
            case String s -> { }   // error: not exhaustive (no default)
        }
    }
}
