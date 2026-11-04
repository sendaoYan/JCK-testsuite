/*
 * @test
 * @summary JLS 15.16 (Java SE 8): a cast expression must convert between
 *          compatible types; casting between unrelated reference and primitive
 *          types is a compile-time error.
 * @compile/fail IncompatibleCast.java
 */

class IncompatibleCast {
    Object m(int x) {
        return (String) x;   // error: inconvertible types; cannot cast int to String
    }
}
