/*
 * @test
 * @summary JLS 11.2.2: a throw statement whose thrown expression has a checked
 *          static type can throw that checked exception class.
 * @compile/fail ThrowStatementThrowsChecked.java
 */

class ThrowStatementThrowsChecked {
    void m() {
        throw new Exception();   // error: unreported checked exception
    }
}
