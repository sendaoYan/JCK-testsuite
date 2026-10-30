/*
 * @test
 * @summary JLS 14.11.1: a case label with more than one case pattern may not
 *          declare any pattern variables.
 * @compile/fail SwitchMultipleCasePatternsFail.java
 */

class SwitchMultipleCasePatternsFail {
    void m(Object o) {
        switch (o) {
            case Integer i, Boolean b -> { }   // error: both declare pattern variables
            default -> { }
        }
    }
}
