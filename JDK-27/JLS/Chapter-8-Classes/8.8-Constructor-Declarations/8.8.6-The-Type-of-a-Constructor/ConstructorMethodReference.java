/*
 * @test
 * @summary JLS 8.8.6: the type of a constructor consists of its signature and
 *          exception types; a constructor method reference selects the matching
 *          constructor by signature.
 * @run main ConstructorMethodReference
 */

import java.util.function.Supplier;

public class ConstructorMethodReference {
    static class Point {
        Point() {}
        Point(int x, int y) {}
    }

    public static void main(String[] args) {
        Supplier<Point> s = Point::new;   // selects the no-arg constructor
        Point p = s.get();
        if (p == null) throw new AssertionError("null point");
        System.out.println("PASS");
    }
}
