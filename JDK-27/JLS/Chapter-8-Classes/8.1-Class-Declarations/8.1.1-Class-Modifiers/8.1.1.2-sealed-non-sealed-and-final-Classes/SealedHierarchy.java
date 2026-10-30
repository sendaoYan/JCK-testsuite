/*
 * @test
 * @summary JLS 8.1.1.2: a sealed class may be extended by its permitted direct
 *          subclasses, which must declare final, sealed, or non-sealed.
 * @run main SealedHierarchy
 */

sealed class Shape permits Circle, Square {}

final class Circle extends Shape {}

non-sealed class Square extends Shape {}

public class SealedHierarchy {
    public static void main(String[] args) {
        Shape c = new Circle();
        Shape s = new Square();
        if (!(c instanceof Shape && s instanceof Shape)) throw new AssertionError("subclassing");
        System.out.println("PASS");
    }
}
