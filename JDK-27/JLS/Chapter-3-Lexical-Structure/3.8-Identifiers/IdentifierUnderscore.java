/*
 * @test
 * @summary JLS 3.8/3.9: the underscore '_' is a reserved keyword and cannot be
 *          used as a one-character identifier (only within identifiers of two
 *          or more characters).
 * @compile/fail IdentifierUnderscore.java
 */

public class IdentifierUnderscore {
    int _ = 0;
}
