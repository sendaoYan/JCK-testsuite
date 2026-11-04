/*
 * @test
 * @summary JLS 6.2 (Java SE 27): the boolean literals true/false and the null
 *          literal are not identifiers (nor keywords), so they cannot be used
 *          as the name of a declared entity.
 * @compile/fail BooleanNullLiteralsNotIdentifiers.java
 */

class BooleanNullLiteralsNotIdentifiers {
    void m() {
        int true = 1;   // error: <identifier> expected
    }
}
