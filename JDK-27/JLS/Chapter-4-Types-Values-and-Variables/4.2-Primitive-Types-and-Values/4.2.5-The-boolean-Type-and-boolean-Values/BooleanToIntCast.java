/*
 * @test
 * @summary JLS 4.2.5: "A boolean value may be cast to type boolean, Boolean,
 *          or Object. No other casts on type boolean are allowed." Casting a
 *          boolean to int must be a compile-time error.
 * @compile/fail BooleanToIntCast.java
 */

class BooleanToIntCast {
    int m(boolean b) {
        return (int) b;   // error: cannot cast boolean to int
    }
}
