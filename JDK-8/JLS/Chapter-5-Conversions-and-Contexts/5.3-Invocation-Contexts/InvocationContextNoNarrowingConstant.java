/*
 * @test
 * @summary JLS 5.3 (Java SE 8): neither strict nor loose invocation contexts
 *          include the implicit narrowing of integer constant expressions which
 *          is allowed in assignment contexts. m(12, 2) with byte/short overloads
 *          is ambiguous/fails because int literals do not narrow in invocation.
 * @compile/fail InvocationContextNoNarrowingConstant.java
 */

class InvocationContextNoNarrowingConstant {
    static int m(byte a, int b) { return a + b; }
    static int m(short a, short b) { return a - b; }

    void test() {
        int r = m(12, 2);   // error: no suitable method found (int literals do not narrow)
    }
}
