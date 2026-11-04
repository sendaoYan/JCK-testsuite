/*
 * @test
 * @summary JLS 3.9: the underscore _ is a keyword (since Java SE 9) and may not
 *          be used as a one-character identifier in any position: field, local
 *          variable, method name, or type name.
 * @compile/fail UnderscoreKeywordContexts.java
 */
public class UnderscoreKeywordContexts {
    int _ = 1;          // field
    void _() {}         // method name
    class _ {}          // nested type name
    void m() {
        int _ = 2;      // local variable
    }
}
