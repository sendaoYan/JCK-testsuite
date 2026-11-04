/*
 * @test
 * @summary JLS 15.21 (Java SE 8): the operands of == and != must be castable to
 *          each other or to a common numeric type; incomparable operands are a
 *          compile-time error.
 * @compile/fail IncompatibleEquality.java
 */

class IncompatibleEquality {
    void test(String s) {
        boolean b = s == 1;   // error: incomparable types: String and int
    }
}
