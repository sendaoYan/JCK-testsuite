/*
 * @test
 * @summary JLS 3.10.5 (Java SE 8): it is a compile-time error for a line
 *          terminator to appear between the quotes of a string literal;
 *          \u000a (LF) and \u000d (CR) are translated to raw line terminators.
 * @compile/fail StringLiteralLineTerminator.java
 */
public class StringLiteralLineTerminator {
    String a = "\u000a";
    String b = "\u000d";
}
