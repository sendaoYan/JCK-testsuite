/*
 * @test
 * @summary JLS 5.3: invocation contexts do NOT include the implicit narrowing
 *          of integer constant expressions allowed in assignment contexts.
 *          m(12, 2) is a compile-time error because the literals have type int
 *          and neither m(byte,int) nor m(short,short) matches (Example 5.3).
 * @compile/fail NoNarrowingInInvocationContext.java
 */

class NoNarrowingInInvocationContext {
    static int m(byte a, int b) { return a + b; }
    static int m(short a, short b) { return a - b; }

    public static void main(String[] args) {
        System.out.println(m(12, 2));   // error: no applicable overload
    }
}
