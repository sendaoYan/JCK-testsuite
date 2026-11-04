/*
 * @test
 * @summary JLS 6.2 (Java SE 27): keywords are not identifiers, so they cannot be
 *          used as the name of a variable, field, method, class, or label.
 * @compile/fail KeywordsNotIdentifiers.java
 */

class KeywordsNotIdentifiers {
    void m() {
        int class = 1;   // error: <identifier> expected (class is a keyword)
    }
}
