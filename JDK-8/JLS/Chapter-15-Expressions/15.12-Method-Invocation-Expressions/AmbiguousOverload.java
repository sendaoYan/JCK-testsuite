/*
 * @test
 * @summary JLS 15.12.2.5 (Java SE 8): "If no method is the most specific, the
 *          method invocation is ambiguous and a compile-time error occurs."
 * @compile/fail AmbiguousOverload.java
 */

class AmbiguousOverload {
    static String m(int x, long y) { return "int,long"; }
    static String m(long x, int y) { return "long,int"; }
    void test() {
        m(1, 1);   // error: reference to m is ambiguous
    }
}
