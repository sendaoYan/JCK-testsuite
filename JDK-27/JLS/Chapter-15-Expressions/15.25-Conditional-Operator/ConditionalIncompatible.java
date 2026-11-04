/*
 * @test
 * @summary JLS 15.25: "It is a compile-time error if either the second or the
 *          third operand expression is an invocation of a void method."
 * @compile/fail ConditionalIncompatible.java
 */

class ConditionalIncompatible {
    static void nothing() { }
    void m(boolean b) {
        Object o = b ? nothing() : 1;   // error: bad type in conditional expression (void operand)
    }
}
