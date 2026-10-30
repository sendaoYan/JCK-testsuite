/*
 * @test
 * @summary JLS 8.4.8.3: an overriding method may not restrict the access of the
 *          method it overrides.
 * @compile/fail OverrideReducesAccess.java
 */

class Point {
    public void move(int dx, int dy) {}
}

class CheckedPoint extends Point {
    protected void move(int dx, int dy) {}   // error: weaker access
}
