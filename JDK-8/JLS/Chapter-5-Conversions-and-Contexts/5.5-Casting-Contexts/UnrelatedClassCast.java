/*
 * @test
 * @summary JLS 5.5/5.5.1 (Java SE 8): if S and T are class types with neither
 *          |S| <: |T| nor |T| <: |S|, a cast from S to T is a compile-time
 *          error (inconvertible types).
 * @compile/fail UnrelatedClassCast.java
 */

class UnrelatedClassCast {
    void m() {
        Object o = new Object();
        // String and Thread are unrelated class types; a cast always fails.
        String s = (String) new Thread();   // error: inconvertible types
    }
}
