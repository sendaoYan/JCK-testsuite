/*
 * @test
 * @summary JLS 14.11.1.1: a switch block over a sealed interface is exhaustive
 *          when it covers every permitted direct subclass, so no default label is
 *          required even for an enhanced switch statement.
 * @run main SwitchExhaustiveSealed
 */

sealed interface Shape permits Circle, Square {}
record Circle(double r) implements Shape {}
record Square(double s) implements Shape {}

public class SwitchExhaustiveSealed {
    static double area(Shape sh) {
        double a;
        switch (sh) {
            case Circle c -> a = 3.14 * c.r() * c.r();
            case Square s -> a = s.s() * s.s();
        }
        return a;   // definitely assigned: the switch is exhaustive
    }

    public static void main(String[] args) {
        if (Math.abs(area(new Circle(1.0)) - 3.14) > 0.01)
            throw new AssertionError("circle area");
        if (area(new Square(2.0)) != 4.0)
            throw new AssertionError("square area");
        System.out.println("PASS");
    }
}
