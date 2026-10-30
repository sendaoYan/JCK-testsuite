/*
 * @test
 * @summary JLS 8.4.3.3: it is a compile-time error to attempt to override or
 *          hide a final method.
 * @compile/fail OverrideFinalMethod.java
 */

class Point {
    final void move(int dx, int dy) {}
}

class OverrideFinalMethod extends Point {
    void move(int dx, int dy) {}   // error: cannot override final method
}
