/*
 * @test
 * @summary JLS 8.2: members of a class that are declared private are not
 *          inherited by subclasses.
 * @compile/fail PrivateMemberNotInherited.java
 */

class Point {
    private static int totalMoves;
    private void reset() {}
}

class Point3d extends Point {
    int z;
    void move() {
        totalMoves++;   // error: totalMoves has private access in Point
        reset();        // error: reset() has private access in Point
    }
}
