/*
 * @test
 * @summary JLS 8.1.4: it is a compile-time error if a class depends on itself
 *          through a circular superclass relationship.
 * @compile/fail CircularClassDependency.java
 */

class Point extends ColoredPoint { int x, y; }
class ColoredPoint extends Point { int color; }
