/*
 * @test
 * @summary JLS 5.5: a cast between unrelated class types Long and Point will
 *          always fail, so it is a compile-time error (Example 5.5-1, error #1).
 * @compile/fail UnrelatedClassCast.java
 */

class Point { int x, y; }

class UnrelatedClassCast {
    void m(Point p) {
        Long l = (Long) p;   // error: Long and Point are unrelated
    }
}
