/*
 * @test
 * @summary JLS 4.2.5 (Java SE 8): "There are no casts between integral types and
 *          the type boolean." A cast of an int to boolean is a compile-time
 *          error (inconvertible types).
 * @compile/fail NoIntegerBooleanCast.java
 */

class NoIntegerBooleanCast {
    void m() {
        int x = 1;
        boolean b = (boolean) x;   // error: inconvertible types; int to boolean
    }
}
