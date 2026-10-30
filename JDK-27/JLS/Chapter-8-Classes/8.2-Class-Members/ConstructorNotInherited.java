/*
 * @test
 * @summary JLS 8.2: constructors are not members and therefore are not inherited.
 * @compile/fail ConstructorNotInherited.java
 */

class Point {
    Point(int x, int y) {}
}

class ColoredPoint extends Point {}

class ConstructorNotInherited {
    void m() {
        new ColoredPoint(0, 0);   // error: no constructor ColoredPoint(int,int)
    }
}
