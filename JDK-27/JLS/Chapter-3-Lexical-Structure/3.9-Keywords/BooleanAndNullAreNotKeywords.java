/*
 * @test
 * @summary JLS 3.9: true, false, and null are not keywords but boolean/null
 *          literals (JLS 3.10.3, 3.10.8); they cannot be used as identifiers.
 * @compile/fail BooleanAndNullAreNotKeywords.java
 */

public class BooleanAndNullAreNotKeywords {
    int true;
    int false;
    int null;
}
