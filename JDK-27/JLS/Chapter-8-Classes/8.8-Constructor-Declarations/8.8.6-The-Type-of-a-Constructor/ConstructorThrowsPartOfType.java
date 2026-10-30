/*
 * @test
 * @summary JLS 8.8.6: the exception types in a constructor's throws clause are
 *          part of the constructor's type, so a constructor that throws a checked
 *          exception is not compatible with a functional interface whose method
 *          does not.
 * @compile/fail ConstructorThrowsPartOfType.java
 */

import java.util.function.Supplier;

class Point {
    Point() throws java.io.IOException {}
}

class ConstructorThrowsPartOfType {
    Supplier<Point> s = Point::new;   // error: unreported exception IOException
}
