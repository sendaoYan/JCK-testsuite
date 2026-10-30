/*
 * @test
 * @summary JLS 9.1.1.4: a sealed interface limits its direct subclasses and
 *          subinterfaces to those named in a permits clause.
 * @run main SealedInterfaceHierarchy
 */

sealed interface Shape permits Circle, Square {}

final class Circle implements Shape {}
final class Square implements Shape {}

public class SealedInterfaceHierarchy {
    public static void main(String[] args) {
        Shape c = new Circle();
        Shape s = new Square();
        if (!(c instanceof Shape) || !(s instanceof Shape)) throw new AssertionError("sealed");
        System.out.println("PASS");
    }
}
