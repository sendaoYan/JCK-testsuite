/*
 * @test
 * @summary JLS 8.1.6: when a sealed class lacks a permits clause, its permitted
 *          direct subclasses are inferred as the classes in the same compilation
 *          unit that specify it as their direct superclass.
 * @run main SealedPermitsInferred
 */

sealed class Shape {}
final class Circle extends Shape {}
non-sealed class Square extends Shape {}

public class SealedPermitsInferred {
    public static void main(String[] args) {
        Shape c = new Circle();
        Shape s = new Square();
        if (!(c instanceof Shape) || !(s instanceof Shape))
            throw new AssertionError("inferred permitted subclasses");
        System.out.println("PASS");
    }
}
