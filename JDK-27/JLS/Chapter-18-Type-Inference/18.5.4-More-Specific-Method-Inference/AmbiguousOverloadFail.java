/*
 * @test
 * @summary JLS 18.5.4 / 15.12.2.5: when two applicable overloads are equally
 *          specific, the invocation is ambiguous and is a compile-time error.
 * @compile/fail AmbiguousOverloadFail.java
 */

class AmbiguousOverloadFail {
    static void m(int i, long l) {}
    static void m(long l, int i) {}

    void x() {
        m(1, 1);   // error: reference to m is ambiguous
    }
}
