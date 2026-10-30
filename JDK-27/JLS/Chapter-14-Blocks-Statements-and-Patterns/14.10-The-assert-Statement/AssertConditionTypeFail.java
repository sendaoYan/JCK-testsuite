/*
 * @test
 * @summary JLS 14.10: Expression1 of an assert statement must have type boolean
 *          or Boolean.
 * @compile/fail AssertConditionTypeFail.java
 */

class AssertConditionTypeFail {
    void m() {
        assert 1;   // error: int cannot be converted to boolean
    }
}
