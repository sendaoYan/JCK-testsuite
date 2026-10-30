/*
 * @test
 * @summary JLS 8.8.7.1: an early construction context restricts references to the
 *          current object, so an instance variable cannot be used as an argument
 *          to an explicit constructor invocation.
 * @compile/fail ConstructorCannotUseInstanceFieldInInvocation.java
 */

class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
}

class ConstructorCannotUseInstanceFieldInInvocation extends Point {
    static final int WHITE = 0;
    int color;
    ConstructorCannotUseInstanceFieldInInvocation(int x, int y) {
        this(x, y, color);   // error: cannot reference color before supertype constructor
    }
    ConstructorCannotUseInstanceFieldInInvocation(int x, int y, int color) {
        super(x, y);
        this.color = color;
    }
}
